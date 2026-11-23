package com.winlator;

/**
 * One fixed, device-specific performance profile. No multi-profile chooser.
 * Workload-dependent game FPS and thermals still require on-device testing.
 */
public final class VexPocoProfileSpec {
    public static final String ID = "poco-x6-pro-mali-g615-v2-smooth";
    public static final String NAME = "POCO X6 Pro • Mali Performance";
    public static final String SCREEN = "960x540";
    public static final String GRAPHICS = "vortek,gladio";
    public static final String DX_WRAPPER = "dxvk";
    public static final String BOX64 = "PERFORMANCE";
    public static final String ENV =
        "MESA_SHADER_CACHE_DISABLE=false MESA_SHADER_CACHE_MAX_SIZE=1024MB " +
        "mesa_glthread=true WINEESYNC=1 WINEDEBUG=-all DXVK_LOG_LEVEL=none";
    private VexPocoProfileSpec() {}

    /** Allow the Android scheduler to distribute Wine, Box64 and graphics
     * threads over ALL online cores. Previously forcing only 4 big cores
     * could starve game/background threads and cause frame-time spikes.
     * This is a single deterministic profile, not a preset selector. */
    public static String cpuAffinity(int cpuCount) {
        if (cpuCount < 1 || cpuCount > 64) return "";
        StringBuilder cores = new StringBuilder();
        for (int i = 0; i < cpuCount; i++) {
            if (i > 0) cores.append(',');
            cores.append(i);
        }
        return cores.toString();
    }
}
