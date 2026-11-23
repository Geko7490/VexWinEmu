# VEX Win Emulator V0.2.1 — Game Library

**Uygulama Ekle → Klasör Seç → EXE Seç → Kütüphaneye Ekle → Oyna**

Android PC-game launcher built on Winlator 11.2 (Wine/Box64) and the original VEX Win workspace. Open the Android Storage Access Framework folder picker, select a Windows game folder and its main EXE. The entire selected folder is copied into the Wine `C:\VexGames` directory, and a `.desktop` launcher is added to the game library. Selecting the game launches Winlator's existing XServerDisplayActivity/Wine execution path, not a simulated UI.

**Limitations:** Import needs free space for a second copy of the game. Some Windows games require launchers, DRM, specific DirectX components or GPUs and may fail. Game performance, especially GTA IV on Mali GPUs, is **not verified**. Create a Winlator container before adding games. The package ID remains `com.winlator` because of upstream bundled native runtime assumptions; back up any existing installation before installing this debug APK.

## V0.2.1 crash fix

Previous V0.2 builds incorrectly stripped `\\` characters from game shortcut paths via Winlator's desktop Exec decoder, so launching a game could crash with `StringIndexOutOfBoundsException` in `FileUtils.getDirname`. V0.2.1 supplies a dedicated, tested decoder for VEX shortcuts, including shortcuts created by older versions; **do not re-import your game** after updating. Invalid paths are rejected at launch rather than crashing the UI.

## APK build

Build via [GitHub Actions](https://github.com/Geko7490/VexWinEmu/actions/workflows/android-runtime.yml). Artifacts are generated only if the build succeeds. To build locally (Android SDK/NDK, JDK 17 required):

    bash scripts/prepare-runtime.sh
    cd work/winlator
    bash gradlew :app:assembleDebug

## Source layout

- `patches/vex-runtime.patch`: old workspace and touch-mode patch against pinned Winlator app revision `a030f552f452158a2db64fdb32b490fa19c0b48d`.
- `overlay/app/src/main/java/com/winlator/VexGameImporter.java`: SAF scan, full-game folder copy, shortcut generation.
- `scripts/install-game-library.py`: inserts V0.2 UI into patched Winlator sources.
- `scripts/prepare-runtime.sh`: restores pinned upstream and overlays.

See [THIRD_PARTY.md](THIRD_PARTY.md) and `LICENSE` for upstream licenses and components.
