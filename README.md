# VEX Win — POCO X6 Pro Edition

VEX Win V0.1.1, Wine ve Box64 ile çalışan Windows oturumuna doğrudan dokunma ve mobil oturum kontrolleri ekler. Winlator 11.2 uygulama kaynakları [a030f55](https://github.com/brunodev85/winlator-app/tree/a030f552f452158a2db64fdb32b490fa19c0b48d) revizyonuna sabitlenmiştir. [VEX Win kaynak değişiklikleri](patches/vex-runtime.patch) derlemeden önce bu revizyona uygulanır.

## APK ve cihaz testi

[GitHub Actions](https://github.com/Geko7490/VexWinEmu/actions/workflows/android-runtime.yml) çalıştırmasındaki **VEX-Win-V0.1.1-Touch-arm64** paketi imzalı debug APK ve SHA-256 özeti içerir. Container ekranından Windows oturumunu aç. Sağ üstteki mor düğme dokunma modunu, Android klavyesini, tam ekranı ve oturum çıkışını açar.

POCO X6 Pro’da Windows masaüstünü aç. Dosya Gezgini’nde dokunma, çift dokunma, uzun basma, pencere taşıma ve iki parmakla kaydırmayı dene. Not Defteri’nde Android klavyesinden Türkçe karakter gir. Tam ekran ve imleç modunu da dene. Kurulum hatası yaşanırsa Android logcat’te `VexWinRuntime` etiketini kontrol et.

Wine, glibc ve grafik bileşenlerindeki sabit çalışma zamanı yolları nedeniyle bu test APK’sı `com.winlator` kimliğini kullanır. Winlator ve önceki VEX Win test APK’sı farklı imzalarla kurulmuş olabilir. Android farklı imzalı paketin üzerine güncelleme yapamaz. Mevcut kurulumunu ve container kayıtlarını korumak için temiz bir test cihazı kullan.

## Kaynaktan derleme

JDK 17, Android SDK 35, Build Tools 34.0.0, NDK 24.0.8215888 ve CMake 3.22.1 ile çalıştır:

```bash
bash scripts/prepare-runtime.sh
cd work/winlator
bash gradlew :app:assembleDebug
```

Hazırlama komutu kaynakları `work/winlator` altında açar. [Lisans ve bileşen kaynakları](THIRD_PARTY.md)
