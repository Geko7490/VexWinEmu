package com.winlator;

import androidx.appcompat.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;
import androidx.documentfile.provider.DocumentFile;
import com.winlator.container.Container;
import com.winlator.core.FileUtils;
import com.winlator.core.StringUtils;
import java.io.*;
import java.util.*;

public final class VexGameImporter {
    private static final int MAX_ENTRIES = 100000;
    private static final long RESERVE = 128L * 1024 * 1024;
    private VexGameImporter() {}

    private static final class Item {
        final DocumentFile source;
        final String path;
        Item(DocumentFile s, String p) { source = s; path = p; }
    }
    private static final class Scan {
        final List<Item> files = new ArrayList<>();
        final List<Item> exe = new ArrayList<>();
        final List<String> dirs = new ArrayList<>();
        long bytes;
        int count;
    }

    public static void importFolder(MainActivity host, Container container, Uri uri) {
        if (uri == null || container == null) return;
        try { host.getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION); }
        catch (SecurityException ignored) {}
        DocumentFile root = DocumentFile.fromTreeUri(host, uri);
        if (root == null || !root.isDirectory()) { error(host, "Klasör açılamadı."); return; }
        AlertDialog dialog = new AlertDialog.Builder(host).setTitle("Klasör taranıyor")
            .setMessage("EXE dosyaları aranıyor…").setCancelable(false).create();
        dialog.show();
        new Thread(() -> {
            try {
                Scan scan = new Scan();
                walk(root, "", 0, scan);
                scan.exe.sort((a, b) -> {
                    int sa = score(a.path), sb = score(b.path);
                    return sa == sb ? a.path.compareToIgnoreCase(b.path) : Integer.compare(sb, sa);
                });
                host.runOnUiThread(() -> {
                    dialog.dismiss();
                    if (host.isFinishing() || host.isDestroyed()) return;
                    if (scan.exe.isEmpty()) { error(host, "Seçilen klasörde EXE bulunamadı."); return; }
                    String[] names = new String[scan.exe.size()];
                    for (int i = 0; i < names.length; i++) names[i] = scan.exe.get(i).path;
                    showExecutablePicker(host, container, root, scan);
                });
            } catch (Exception ex) {
                host.runOnUiThread(() -> { dialog.dismiss(); if (!host.isFinishing()) error(host, ex.getMessage()); });
            }
        }, "vex-scan").start();
    }

    /** Keep the executable list separate from the copy explanation.
     * Android AlertDialog cannot display a message and list simultaneously.
     */
    private static void showExecutablePicker(MainActivity host, Container container, DocumentFile root, Scan scan) {
        if (host.isFinishing() || host.isDestroyed()) return;
        String[] names = new String[scan.exe.size()];
        for (int i = 0; i < names.length; i++) names[i] = scan.exe.get(i).path;
        new AlertDialog.Builder(host)
            .setTitle("Ana EXE dosyasını seç • " + size(scan.bytes))
            .setSingleChoiceItems(names, -1, (dialog, which) -> {
                dialog.dismiss();
                Item chosen = scan.exe.get(which);
                new AlertDialog.Builder(host)
                    .setTitle("Oyunu içe aktar")
                    .setMessage("EXE: " + chosen.path +
                        "\\n\\nTüm klasör Wine C: diskine kopyalanacak (yaklaşık " +
                        size(scan.bytes) + "). Telefonunda ek depolama alanı gerekir.")
                    .setNegativeButton("Geri", (confirmation, button) ->
                        showExecutablePicker(host, container, root, scan))
                    .setPositiveButton("İçe Aktar", (confirmation, button) ->
                        copy(host, container, scan, chosen, clean(root.getName())))
                    .show();
            })
            .setNegativeButton(android.R.string.cancel, null)
            .show();
    }

    private static void walk(DocumentFile dir, String parent, int depth, Scan scan) throws IOException {
        if (depth > 24) throw new IOException("Klasör derinliği çok fazla.");
        for (DocumentFile f : dir.listFiles()) {
            if (++scan.count > MAX_ENTRIES) throw new IOException("Çok fazla dosya var.");
            String path = parent.isEmpty() ? clean(f.getName()) : parent + "/" + clean(f.getName());
            if (f.isDirectory()) { scan.dirs.add(path); walk(f, path, depth + 1, scan); }
            else if (f.isFile()) {
                Item item = new Item(f, path);
                scan.files.add(item);
                scan.bytes += Math.max(0, f.length());
                if (path.toLowerCase(Locale.ROOT).endsWith(".exe")) scan.exe.add(item);
            }
        }
    }

    private static int score(String path) {
        String n = path.substring(path.lastIndexOf('/') + 1).toLowerCase(Locale.ROOT);
        int s = path.contains("/") ? 0 : 30;
        if (n.contains("gta") || n.contains("game")) s += 10;
        if (n.contains("setup") || n.contains("unins") || n.contains("crash") ||
            n.contains("redist") || n.contains("config") || n.contains("launcher")) s -= 30;
        return s;
    }

    private static void copy(MainActivity host, Container container, Scan scan, Item selected, String gameName) {
        AlertDialog progress = new AlertDialog.Builder(host).setTitle("Oyun aktarılıyor")
            .setMessage("Dosyalar kopyalanıyor. Uygulamayı kapatma.").setCancelable(false).create();
        progress.show();
        new Thread(() -> {
            File pending = null, finalDir = null;
            boolean success = false;
            try {
                File c = new File(container.getRootDir(), ".wine/drive_c");
                if (!c.isDirectory()) throw new IOException("Wine C: diski bulunamadı.");
                File games = new File(c, "VexGames");
                if (!games.isDirectory() && !games.mkdirs()) throw new IOException("Oyun klasörü oluşturulamadı.");
                if (scan.bytes > 0 && games.getUsableSpace() < scan.bytes + RESERVE)
                    throw new IOException("Depolama alanı yetersiz.");
                String name = gameName;
                int n = 2;
                while (new File(games, name).exists() || new File(games, "." + name + ".partial").exists())
                    name = gameName + "-" + n++;
                pending = new File(games, "." + name + ".partial");
                finalDir = new File(games, name);
                if (!pending.mkdirs()) throw new IOException("Geçici klasör açılamadı.");
                for (String path : scan.dirs) {
                    File d = new File(pending, path);
                    if (!d.isDirectory() && !d.mkdirs()) throw new IOException("Klasör açılamadı: " + path);
                }
                long copied = 0;
                int done = 0;
                for (Item item : scan.files) {
                    File target = new File(pending, item.path);
                    if (!target.getParentFile().isDirectory() && !target.getParentFile().mkdirs())
                        throw new IOException("Hedef klasör oluşturulamadı.");
                    try (InputStream in = host.getContentResolver().openInputStream(item.source.getUri());
                         FileOutputStream out = new FileOutputStream(target)) {
                        if (in == null) throw new IOException("Dosya okunamadı: " + item.path);
                        byte[] buf = new byte[65536];
                        int len;
                        while ((len = in.read(buf)) != -1) { out.write(buf, 0, len); copied += len; }
                    }
                    done++;
                    if (done % 8 == 0 || done == scan.files.size()) {
                        final int count = done;
                        final long bytes = copied;
                        host.runOnUiThread(() -> {
                            if (!host.isFinishing() && !host.isDestroyed())
                                progress.setMessage(count + "/" + scan.files.size() + " • " +
                                    size(bytes) + " / " + size(scan.bytes));
                        });
                    }
                }
                if (!pending.renameTo(finalDir)) throw new IOException("Aktarım tamamlanamadı.");
                pending = null;
                shortcut(container, finalDir, selected.path, name);
                success = true;
            } catch (Exception ex) {
                String message = ex.getMessage();
                host.runOnUiThread(() -> {
                    progress.dismiss();
                    if (!host.isFinishing() && !host.isDestroyed()) error(host, message);
                });
            } finally {
                if (!success) {
                    if (pending != null) FileUtils.delete(pending);
                    if (finalDir != null) FileUtils.delete(finalDir);
                }
            }
            if (success) host.runOnUiThread(() -> {
                progress.dismiss();
                if (!host.isFinishing() && !host.isDestroyed()) {
                    Toast.makeText(host, "Oyun kütüphaneye eklendi.", Toast.LENGTH_LONG).show();
                    host.showFragment(new VexWorkspaceFragment());
                }
            });
        }, "vex-import").start();
    }

    private static void shortcut(Container c, File installed, String exe, String title) throws IOException {
        File desktop = new File(c.getUserDir(), "Desktop");
        if (!desktop.isDirectory() && !desktop.mkdirs()) throw new IOException("Kısayol klasörü oluşturulamadı.");
        File file = new File(desktop, title + ".desktop");
        int n = 2;
        while (file.exists()) file = new File(desktop, title + "-" + n++ + ".desktop");
        String dos = "C:\\VexGames\\" + installed.getName() + "\\" + exe.replace('/', '\\');
        FileUtils.writeString(file, "[Desktop Entry]\nType=Application\nName=" + title +
            "\nExec=wine " + StringUtils.escapeDOSPath(dos) +
            "\nTerminal=false\nCategories=Game;\n");
        if (!file.isFile() || file.length() == 0) throw new IOException("Kısayol yazılamadı.");
    }

    private static String clean(String name) {
        String s = name == null ? "" : name.trim();
        s = s.replace('/', '_').replace('\\', '_');
        if (s.isEmpty() || s.equals(".") || s.equals("..")) s = "Game";
        return s.length() > 100 ? s.substring(0, 100) : s;
    }
    private static String size(long v) {
        return v > 1073741824L ? String.format(Locale.ROOT, "%.1f GB", v / 1073741824.0)
            : String.format(Locale.ROOT, "%.1f MB", v / 1048576.0);
    }
    private static void error(MainActivity host, String message) {
        new AlertDialog.Builder(host).setTitle("Aktarım hatası")
            .setMessage(message == null ? "Bilinmeyen hata." : message)
            .setPositiveButton(android.R.string.ok, null).show();
    }
}
