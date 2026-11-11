# VEX Win — POCO X6 Pro Edition

VEX Win V0.1 Runtime, Android üzerinde Wine ve Box64 ile Windows uygulamalarını çalıştırmak için hazırlanan ilk cihaz testi sürümüdür. Winlator 11.2 uygulama kaynakları [a030f55](https://github.com/brunodev85/winlator-app/tree/a030f552f452158a2db64fdb32b490fa19c0b48d) revizyonuna sabitlenmiştir. [VEX Win kaynak değişiklikleri](patches/vex-runtime.patch) derlemeden önce bu revizyona uygulanır.

## APK ve cihaz testi

[GitHub Actions](https://github.com/Geko7490/VexWinEmu/actions/workflows/android-runtime.yml) çalıştırmasındaki **VEX-Win-V0.1-Runtime-arm64** paketi imzalı debug APK ve SHA-256 özeti içerir. Bu sürüm Winlator’ın container ekranını kullanır.

İlk cihaz testi için POCO X6 Pro’da uygulamayı aç, ilk kurulumu tamamla, bir container oluştur ve masaüstünü başlat. Ardından oturumu kapatıp aynı container’ı tekrar aç. Kurulum hatası yaşanırsa Android logcat’te `VexWinRuntime` etiketini kontrol et.

Wine, glibc ve grafik bileşenlerindeki sabit çalışma zamanı yolları nedeniyle bu test APK’sı `com.winlator` kimliğini kullanır. Farklı imzalı Winlator kuruluysa Android bu APK’yı onun üzerine veya yanına kuramaz. Mevcut kurulumunu ve container kayıtlarını korumak için testte Winlator kurulu olmayan bir cihaz kullan.

## Kaynaktan derleme

JDK 17, Android SDK 35, Build Tools 34.0.0, NDK 24.0.8215888 ve CMake 3.22.1 ile çalıştır:

```bash
bash scripts/prepare-runtime.sh
cd work/winlator
bash gradlew :app:assembleDebug
```

Hazırlama komutu kaynakları `work/winlator` altında açar. [Lisans ve bileşen kaynakları](THIRD_PARTY.md)
