package com.winlator;

import android.os.Build;
import com.winlator.box64.Box64Preset;
import com.winlator.container.Container;
import com.winlator.container.GraphicsDrivers;
import com.winlator.container.DXWrappers;

/**
 * Applies one consistent Mali-G615/Dimensity 8300 Ultra profile.
 * Only known performance controls are changed; game files, drive mappings,
 * Wine prefix, audio and saved shortcuts are left untouched.
 */
public final class VexPocoProfile {
    private VexPocoProfile() {}

    public static boolean isTargetDevice() {
        String device = Build.DEVICE == null ? "" : Build.DEVICE;
        String model = Build.MODEL == null ? "" : Build.MODEL;
        return "duchamp".equalsIgnoreCase(device) ||
            "2311DRK48G".equalsIgnoreCase(model) ||
            "2311DRK48C".equalsIgnoreCase(model) ||
            "2311DRK48I".equalsIgnoreCase(model) ||
            model.toLowerCase(java.util.Locale.ROOT).contains("poco x6 pro");
    }

    public static boolean apply(Container c) {
        if (c == null) return false;
        boolean changed = false;
        changed |= replaceScreen(c);
        changed |= replaceGraphics(c);
        changed |= replaceWrapper(c);
        changed |= replaceBox64(c);
        changed |= replaceCPU(c);
        changed |= replaceEnv(c);
        if (!VexPocoProfileSpec.ID.equals(c.getExtra("vex_poco_profile"))) {
            c.putExtra("vex_poco_profile", VexPocoProfileSpec.ID);
            changed = true;
        }
        return changed;
    }

    private static boolean replaceScreen(Container c) {
        if (VexPocoProfileSpec.SCREEN.equals(c.getScreenSize())) return false;
        c.setScreenSize(VexPocoProfileSpec.SCREEN);
        return true;
    }

    private static boolean replaceGraphics(Container c) {
        boolean changed = false;
        if (!VexPocoProfileSpec.GRAPHICS.equals(c.getGraphicsDriver())) {
            c.setGraphicsDriver(VexPocoProfileSpec.GRAPHICS);
            changed = true;
        }
        // Use the tested Winlator upstream Vortek/Gladio defaults, no Turnip.
        if (!"".equals(c.getGraphicsDriverConfig())) {
            c.setGraphicsDriverConfig("");
            changed = true;
        }
        return changed;
    }

    private static boolean replaceWrapper(Container c) {
        boolean changed = false;
        if (!DXWrappers.DXVK.equals(c.getDXWrapper())) {
            c.setDXWrapper(DXWrappers.DXVK);
            changed = true;
        }
        if (!"".equals(c.getDXWrapperConfig())) {
            c.setDXWrapperConfig("");
            changed = true;
        }
        return changed;
    }

    private static boolean replaceBox64(Container c) {
        if (Box64Preset.PERFORMANCE.equals(c.getBox64Preset())) return false;
        c.setBox64Preset(Box64Preset.PERFORMANCE);
        return true;
    }

    private static boolean replaceCPU(Container c) {
        String list = VexPocoProfileSpec.cpuAffinity(Runtime.getRuntime().availableProcessors());
        boolean changed = false;
        if (!list.equals(c.getCPUList())) {
            c.setCPUList(list);
            changed = true;
        }
        if (!list.equals(c.getCPUListWoW64())) {
            c.setCPUListWoW64(list);
            changed = true;
        }
        return changed;
    }

    private static boolean replaceEnv(Container c) {
        if (VexPocoProfileSpec.ENV.equals(c.getEnvVars())) return false;
        c.setEnvVars(VexPocoProfileSpec.ENV);
        return true;
    }
}
