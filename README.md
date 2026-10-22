# Coin Miner (AdMob Rewarded)

Konsep: tonton iklan rewarded -> dapat 100 koin -> beli upgrade perlengkapan mining
(menambah koin pasif per detik saat aplikasi dibuka).

## Alur iklan
- Pengguna menekan tombol "Tonton iklan". Iklan TIDAK muncul otomatis (aturan AdMob: rewarded ad harus atas pilihan pengguna).
- Koin diberikan hanya jika iklan ditonton sampai reward terpenuhi.
- Setelah iklan selesai, tombol aktif lagi setelah 10 detik (COOLDOWN_MS).

## Mengganti ID AdMob
- App ID: app/src/main/AndroidManifest.xml
- Rewarded Ad Unit ID: MainActivity.kt (REWARDED_AD_UNIT_ID)
Buat entri aplikasi BARU di dashboard AdMob untuk app ini (package com.example.coinminer, atau ganti package-nya),
lalu pakai App ID dan Ad Unit ID dari entri itu. Default saat ini adalah ID TEST Google.
JANGAN klik iklan asli milikmu sendiri.

## Build APK
A. Android Studio: File > Open folder ini, tunggu sync, Build > Build APK(s).
   Hasil: app/build/outputs/apk/debug/app-debug.apk
B. GitHub Actions: upload folder ke repo, tab Actions > Build APK > Run workflow, unduh dari Artifacts.
