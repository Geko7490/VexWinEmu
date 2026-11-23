#!/usr/bin/env python3
from pathlib import Path
import shutil
import sys

repo = Path(__file__).resolve().parents[1]
root = Path(sys.argv[1]).resolve()

def replace_once(path, old, new):
    p = root / path
    text = p.read_text(encoding="utf-8")
    if text.count(old) != 1:
        raise RuntimeError("Patch target missing or ambiguous: " + path + " [" + old[:50] + "]")
    p.write_text(text.replace(old, new, 1), encoding="utf-8")

main = "app/src/main/java/com/winlator/MainActivity.java"
replace_once(main, "    public void setOpenFileCallback(Callback<Uri> openFileCallback) {",
'''    public static final int VEX_IMPORT_FOLDER_REQUEST = 46;

    public void importGameFolder() {
        if (!RootFS.find(this).isValid()) {
            android.widget.Toast.makeText(this, R.string.vex_workspace_wait, android.widget.Toast.LENGTH_LONG).show();
            return;
        }
        Container container = workspaceContainer(new ContainerManager(this).getContainers());
        if (container == null) {
            createWorkspaceContainer();
            return;
        }
        Intent pick = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        pick.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(pick, VEX_IMPORT_FOLDER_REQUEST);
    }

    public void setOpenFileCallback(Callback<Uri> openFileCallback) {''')

replace_once(main, "        super.onActivityResult(requestCode, resultCode, data);",
'''        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VEX_IMPORT_FOLDER_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Container container = workspaceContainer(new ContainerManager(this).getContainers());
            if (container != null) VexGameImporter.importFolder(this, container, data.getData());
            return;
        }''')

workspace = "app/src/main/java/com/winlator/VexWorkspaceFragment.java"
replace_once(workspace, "        LinearLayout entryPoints = new LinearLayout(context);",
'''        Button importGame = button(R.string.vex_game_add, true);
        importGame.setOnClickListener(view -> activity().importGameFolder());
        add(content, importGame, 10);

        LinearLayout entryPoints = new LinearLayout(context);''')

build = "app/build.gradle"
replace_once(build, "versionCode 35", "versionCode 38")
replace_once(build, 'versionName "0.1.2-workspace"', 'versionName "0.3.0-poco"')
replace_once(build, "    implementation 'androidx.appcompat:appcompat:1.4.0'",
             "    implementation 'androidx.appcompat:appcompat:1.4.0'\n    implementation 'androidx.documentfile:documentfile:1.0.1'")

# Fix imported Winlator desktop shortcuts (including games already imported in V0.2).
shortcut = "app/src/main/java/com/winlator/container/Shortcut.java"
replace_once(shortcut,
    '            index = path.indexOf("start.exe ");',
    '''            String vexGamePath = com.winlator.VexShortcutPath.decodeExec(execArgs);
            if (vexGamePath != null) path = vexGamePath;
            index = path.indexOf("start.exe ");''')

# Never crash while resolving malformed third-party shortcut paths.
display = "app/src/main/java/com/winlator/XServerDisplayActivity.java"
replace_once(display,
    '        if (execPath != null) {\n            String execDir = FileUtils.getDirname(execPath);',
    '''        if (execPath != null &&
            Math.max(execPath.lastIndexOf('/'), execPath.lastIndexOf('\\\\')) < 0) {
            android.util.Log.e("VexWinRuntime", "Ignoring malformed Windows EXE path: " + execPath);
            execPath = null;
        }
        if (execPath != null) {
            String execDir = FileUtils.getDirname(execPath);''')


# One POCO X6 Pro performance profile for all Wine containers. Applies to
# both new and existing containers without touching game files or drive data.
manager = "app/src/main/java/com/winlator/container/ContainerManager.java"
replace_once(manager,
    "                            container.loadData(data);\n                            containers.add(container);",
    "                            container.loadData(data);\n" +
    "                            if (com.winlator.VexPocoProfile.apply(container)) container.saveData();\n" +
    "                            containers.add(container);")
replace_once(manager,
    "            container.loadData(data);\n\n            boolean isMainWineVersion",
    "            container.loadData(data);\n" +
    "            com.winlator.VexPocoProfile.apply(container);\n\n" +
    "            boolean isMainWineVersion")
replace_once(manager,
    "        dstContainer.setDesktopTheme(srcContainer.getDesktopTheme());\n        dstContainer.saveData();",
    "        dstContainer.setDesktopTheme(srcContainer.getDesktopTheme());\n" +
    "        com.winlator.VexPocoProfile.apply(dstContainer);\n        dstContainer.saveData();")

# Advanced container UI remains for diagnostics but the saved execution
# profile is fixed to the same device preset.
detail = "app/src/main/java/com/winlator/ContainerDetailFragment.java"
replace_once(detail,
    "                    container.setDesktopTheme(desktopTheme);\n                    container.saveData();",
    "                    container.setDesktopTheme(desktopTheme);\n" +
    "                    com.winlator.VexPocoProfile.apply(container);\n                    container.saveData();")

# No multi-profile wizard: create the first container with fixed settings
# automatically, so +Add Game takes the user straight to a ready launcher.
replace_once(main,
    "    public void createWorkspaceContainer() {\n        if (!workspaceRuntimeReady()) return;\n        showFragment(new ContainerDetailFragment());\n    }",
    '''    public void createWorkspaceContainer() {
        if (!workspaceRuntimeReady()) return;
        ContainerManager manager = new ContainerManager(this);
        Container existing = workspaceContainer(manager.getContainers());
        if (existing != null) {
            selectWorkspaceContainer(existing.id);
            showFragment(new VexWorkspaceFragment());
            return;
        }
        org.json.JSONObject config = new org.json.JSONObject();
        try { config.put("name", "POCO X6 Pro"); }
        catch (org.json.JSONException ignored) {}
        preloaderDialog.show(R.string.creating_container);
        manager.createContainerAsync(config, created -> {
            preloaderDialog.close();
            if (isFinishing() || isDestroyed()) return;
            if (created == null) {
                android.widget.Toast.makeText(this, "Wine container creation failed", android.widget.Toast.LENGTH_LONG).show();
                return;
            }
            selectWorkspaceContainer(created.id);
            showFragment(new VexWorkspaceFragment());
        });
    }''')

# Device-specific profile label replaces the previously generic details.
replace_once(workspace,
    '        containerPicker.setVisibility(containers.size() > 1 ? View.VISIBLE : View.GONE);',
    '        containerPicker.setVisibility(View.GONE);')
replace_once(workspace,
    '''        containerDetails.setText(getString(R.string.vex_workspace_meta,
            WineInfo.fromIdentifier(requireContext(), container.getWineVersion()).toString(), container.getScreenSize()));''',
    '''        containerDetails.setText("Tek profil • POCO X6 Pro / Mali-G615 • 960x540 • Vortek + DXVK");''')

for p in (repo / "overlay").rglob("*"):
    if p.is_file():
        dst = root / p.relative_to(repo / "overlay")
        dst.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(p, dst)
print("VEX game library installed")
