import com.winlator.VexShortcutPath;
public final class VexShortcutPathTest {
    private static String esc(String path) {
        return path.replace("\\", "\\\\").replace(" ", "\\ ");
    }
    private static void verify(String expected) {
        String input = "wine " + esc(expected);
        String parsed = VexShortcutPath.decodeExec(input);
        if (!expected.equals(parsed))
            throw new AssertionError("Expected [" + expected + "], got [" + parsed + "], from " + input);
    }
    public static void main(String[] args) {
        verify("C:\\VexGames\\GTA 4\\GTAIV.exe");
        verify("C:\\VexGames\\Test\\bin\\game.exe");
        verify("C:\\VexGames\\NFS Most Wanted\\speed.exe");
        verify("C:\\VexGames\\Deneme Oyunu\\Oyunum.exe");
        verify("C:\\VexGames\\folder\\another folder\\Game.exe");
        if (VexShortcutPath.decodeExec("wine C:\\\\Program Files\\\\SomeApp.exe") != null)
            throw new AssertionError("Non VEX shortcut changed");
        if (VexShortcutPath.decodeExec("wine C:VexGamesGTA4GTAIV.exe") != null)
            throw new AssertionError("Corrupt path accepted");
        if (VexShortcutPath.decodeExec("wine C:\\\\VexGames\\\\x\\\\file.txt") != null)
            throw new AssertionError("Non EXE accepted");
        System.out.println("VexShortcutPathTest OK (5 valid + 3 invalid)");
    }
}
