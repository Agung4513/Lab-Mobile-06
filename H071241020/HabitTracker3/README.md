# Habit Tracker Pro - Tugas Final Lab Mobile 2026

Habit Tracker Pro adalah aplikasi manajemen kebiasaan (habit) yang dirancang untuk membantu pengguna membangun disiplin diri melalui pencatatan target harian, pemantauan statistik, dan interaksi komunitas. Aplikasi ini dikembangkan sebagai syarat pemenuhan Tugas Final Lab Mobile 2026.

## 📱 Fitur Utama

- **Pelacakan Habit Harian**: Catat kebiasaan berdasarkan hari, prioritas (Urgent, Tinggi, Sedang, Rendah), dan tingkat kesulitan.
- **Statistik & Progress**: Visualisasi tingkat penyelesaian habit harian, jumlah sesi total, dan rekor *streak* terlama.
- **Sistem Pencapaian (Badges)**: Dapatkan medali/badge saat mencapai target tertentu (misal: 100 kali check-in atau 3 hari streak).
- **Eksplorasi & Motivasi**: Kutipan harian dari API publik untuk menjaga motivasi pengguna.
- **Komunitas Sosial**: Bagikan progres atau pemikiran dalam bentuk postingan di feed komunitas lokal.
- **Manajemen Profil**: Personalisasi akun dengan nama, bio, dan avatar unik (emoji atau foto galeri).
- **Tema Dinamis**: Mendukung mode Terang (Light) dan Gelap (Dark).

## 🛠️ Implementasi Teknis

Aplikasi ini dibangun menggunakan spesifikasi teknis berikut:

1.  **Architecture**: Java dengan pola desain modern (View Binding).
2.  **Navigation**: Menggunakan **Navigation Component** untuk transisi antar fragment (`Home`, `Stats`, `Explore`, `Profile`).
3.  **Local Persistence**: 
    - **SQLite**: Menyimpan data habit, riwayat check-in, dan postingan komunitas.
    - **SharedPreferences**: Menyimpan preferensi pengguna (Tema, Badge yang terbuka, dan data profil).
4.  **Networking**: Menggunakan **Retrofit** dan **Moshi** untuk mengambil kutipan motivasi dari API publik.
5.  **Concurrency**: Operasi database dilakukan di *background thread* menggunakan **ExecutorService** untuk menjaga kelancaran UI (mencegah ANR).
6.  **UI/UX**: Menggunakan **Material 3 Design**, **RecyclerView** dengan berbagai adapter, dan kustom animasi dialog.

## 🚀 Cara Penggunaan

1.  **Tambah Habit**: Klik tombol `+` di Home. Masukkan judul, pilih hari pelaksanaan, prioritas, dan jam (opsional).
2.  **Check-in**: Pada daftar habit hari ini, klik tombol centang untuk menyelesaikan tugas.
3.  **Hapus/Edit**: Tekan lama pada salah satu habit di daftar untuk mengedit atau menghapusnya.
4.  **Ubah Tema**: Secara otomatis mengikuti sistem atau dapat disesuaikan melalui pengaturan profil (Dark/Light mode).
5.  **Eksplorasi**: Geser ke tab Explore untuk melihat kutipan hari ini. Jika tidak ada internet, klik "Coba Lagi" untuk memuat ulang.

## 📁 Struktur Proyek

- `MainActivity`: Entry point aplikasi dengan Bottom Navigation.
- `AddHabitActivity`: Form untuk membuat/mengedit kebiasaan.
- `EditProfileActivity`: Pengaturan identitas pengguna.
- `Fragments`: Logika tampilan utama (`Home`, `Stats`, `Explore`, `Profile`).
- `DatabaseHelper`: Manajemen database SQLite.
- `Retrofit Client`: Penanganan komunikasi API.

---
**Disusun Oleh:**
[Agung Allo Karaeng] - [H071241020]
Tugas Final Praktikum Mobile 2026
