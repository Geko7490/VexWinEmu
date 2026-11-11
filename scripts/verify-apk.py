import sys
from zipfile import ZipFile

required = {
    "assets/rootfs.tzst",
    "assets/container_pattern.tzst",
    "assets/box64/box64-0.4.4.tzst",
    "assets/graphics_driver/vortek-2.1.tzst",
    "assets/graphics_driver/virgl-23.1.9.tzst",
    "assets/dxwrapper/dxvk-1.10.3.tzst",
    "assets/dxwrapper/dxvk-2.4.1.tzst",
    "assets/licenses/Winlator-LGPL-2.1.txt",
    "lib/arm64-v8a/libwinlator.so",
    "lib/arm64-v8a/libvortekrenderer.so",
    "lib/arm64-v8a/libvirglrenderer.so",
}

with ZipFile(sys.argv[1]) as apk:
    names = set(apk.namelist())
    missing = sorted(name for name in required if name not in names or apk.getinfo(name).file_size == 0)
    if missing:
        raise SystemExit("Missing or empty runtime components:\n" + "\n".join(missing))
    other_abis = {name.split("/")[1] for name in names if name.startswith("lib/") and name.endswith(".so")} - {"arm64-v8a"}
    if other_abis:
        raise SystemExit("Unexpected native ABIs: " + ", ".join(sorted(other_abis)))
    print(f"Verified {len(required)} runtime entries and ARM64 ABI")
