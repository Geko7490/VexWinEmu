package com.winlator;

/**
 * One fixed, device-specific performance profile. No multi-profile chooser.
 * Workload-dependent game FPS and thermals still require on-device testing.
 */
public final class VexPocoProfileSpec {
    public static final String ID = "poco-x6-pro-mali-g615-v1";
    public static final String NAME = "POCO X6 Pro • Mali Performance";
    public static final String SCREEN = "960x540";
    public static final String GRAPHICS = "vortek,gladio";
    public static final String DX_WRAPPER = "dxvk";
    public static final String BOX64 = "PERFORMANCE";
    public static final String ENV =
        "MESA_SHADER_CACHE_DISABLE=false MESA_SHADER_CACHE_MAX_SIZE=1024MB " +
        "mesa_glthread=true WINEESYNC=1 WINEDEBUG=-all DXVK_LOG_LEVEL=none";
    private VexPocoProfileSpec() {}

    /** On POCO X6 Pro the faster A715 cores are normally 4..7.
     * If fewer CPUs are online, leave affinity unpinned for safety. */
    public static String cpuAffinity(int cpuCount) {
        if (cpuCount < 8) return "";
        return "4,5,6,7";
    }
}
