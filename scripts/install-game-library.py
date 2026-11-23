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
replace_once(build, "versionCode 35", "versionCode 37")
replace_once(build, 'versionName "0.1.2-workspace"', 'versionName "0.2.1-pathfix"')
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

for p in (repo / "overlay").rglob("*"):
    if p.is_file():
        dst = root / p.relative_to(repo / "overlay")
        dst.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(p, dst)
print("VEX game library installed")
