package com.winlator;

import java.util.Locale;

/**
 * Reconstructs an imported Windows C: drive path from a Winlator .desktop
 * Exec=wine entry. Unlike StringUtils.unescapeDOSPath(), this decoder retains
 * directory separators. All V0.2 shortcuts are backwards compatible.
 */
public final class VexShortcutPath {
    private static final String PREFIX = "C:\\VexGames\\";
    private VexShortcutPath() {}

    /** Returns null for non-VEX shortcuts so their upstream parser is preserved. */
    public static String decodeExec(String exec) {
        if (exec == null || !exec.startsWith("wine ")) return null;
        String encoded = exec.substring(5).trim();
        StringBuilder decoded = new StringBuilder(encoded.length());
        for (int i = 0; i < encoded.length(); i++) {
            char ch = encoded.charAt(i);
            if (ch == '\\' && i + 1 < encoded.length()) {
                char next = encoded.charAt(i + 1);
                if (next == '\\' || next == ' ') {
                    decoded.append(next);
                    i++;
                    continue;
                }
            }
            decoded.append(ch);
        }
        String path = decoded.toString();
        if (!path.regionMatches(true, 0, PREFIX, 0, PREFIX.length())) return null;
        if (!path.toLowerCase(Locale.ROOT).endsWith(".exe")) return null;
        if (path.indexOf('\\', PREFIX.length()) <= PREFIX.length()) return null;
        return path;
    }
}
