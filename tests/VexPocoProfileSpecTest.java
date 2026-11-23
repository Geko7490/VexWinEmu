import com.winlator.VexPocoProfileSpec;
public final class VexPocoProfileSpecTest {
    public static void main(String[] args) {
        check("960x540".equals(VexPocoProfileSpec.SCREEN), "resolution");
        check("vortek,gladio".equals(VexPocoProfileSpec.GRAPHICS), "Mali driver");
        check("dxvk".equals(VexPocoProfileSpec.DX_WRAPPER), "DX backend");
        check("PERFORMANCE".equals(VexPocoProfileSpec.BOX64), "Box64");
        check("0,1,2,3,4,5,6,7".equals(VexPocoProfileSpec.cpuAffinity(8)), "all CPU cores for smooth scheduling");
        check("0,1,2,3".equals(VexPocoProfileSpec.cpuAffinity(4)), "4-core safety fallback");
        check("".equals(VexPocoProfileSpec.cpuAffinity(0)), "invalid core count");
        check(VexPocoProfileSpec.ENV.contains("MESA_SHADER_CACHE_MAX_SIZE=1024MB"), "shader cache");
        check(!VexPocoProfileSpec.ENV.contains("TU_DEBUG"), "no Adreno flags");
        System.out.println("POCO X6 Pro single profile tests passed (9 assertions)");
    }
    private static void check(boolean pass, String what) {
        if (!pass) throw new AssertionError(what);
    }
}
