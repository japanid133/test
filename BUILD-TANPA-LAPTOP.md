# Build APK tanpa install apa pun di laptop (hanya browser)

1. Buka github.com, login (atau daftar gratis).
2. Klik "New repository", beri nama bebas (mis. coinminer), pilih Public atau Private, klik Create.
3. Ekstrak CoinMiner.zip di HP/laptop, lalu di halaman repo klik "uploading an existing file".
   Seret SEMUA isi folder CoinMiner (termasuk folder .github, app, dan file .kts) lalu klik Commit changes.
   Catatan: folder ".github" tersembunyi di beberapa sistem. Pastikan ikut terunggah.
4. Buka tab "Actions". Jika diminta, klik "I understand my workflows, enable them".
5. Pilih "Build APK" di kiri, klik "Run workflow". Tunggu sekitar 3-6 menit sampai centang hijau.
6. Klik run yang selesai, scroll ke bagian "Artifacts", unduh "CoinMiner-apk" (zip berisi app-debug.apk).
7. Kirim app-debug.apk ke HP Android, izinkan "Install dari sumber tidak dikenal", lalu install.

Semua proses build berjalan di server GitHub, jadi tidak memakai storage laptopmu.
