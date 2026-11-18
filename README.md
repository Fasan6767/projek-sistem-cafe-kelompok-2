# ☕ Sistem Kasir Kafe KopiKita

Repositori ini berisi sistem aplikasi manajemen kafe berbasis konsol (CLI) yang dikembangkan menggunakan Kotlin dan Gradle. Proyek ini dirancang untuk menangani alur pemesanan menu, pengelolaan katalog, perhitungan omzet harian, serta proses pembayaran tunai maupun simulasi QRIS.

---

## 👥 Informasi Tim & Peran

* **Proyek:** Sistem Kafe KopiKita (Kelompok 2)
* **QA Engineer:** Nasywan Aziz (NIM: 225443048)
* **Status Proyek:** Tahap Pengujian Unit Test & Penyelarasan Dokumen

---

## 🛠️ Prasyarat & Lingkungan Pengembangan (Environment)

Untuk menghindari kendala kompiler, pastikan perangkat Anda menggunakan perangkat lunak berikut:

* **Java Development Kit (JDK):** Temurin JDK 21 (Direkomendasikan untuk kompatibilitas kompiler Kotlin proyek).
* **Build Tool:** Gradle (menggunakan `./gradlew` bawaan repositori).

---

## 📋 Ringkasan Laporan & Temuan QA

Berdasarkan hasil audit sistem terbaru yang dilakukan oleh QA Engineer, berikut adalah poin-poin penting temuan serta rekomendasi perbaikannya:

### 1. Perbaikan Kompilasi `OrderWorkflowTest.kt`
* Menghapus karakter teks asing pada baris pertama file pengujian agar deklarasi `package com.kopikita` terbaca di baris paling atas.
* Menyelaraskan argumen konstruktor pembayaran tunai dari `uangDiterima` menjadi `uangDibayar` sesuai dengan kelas `MetodePembayaran.kt`.
* Mengganti pemanggilan method `hitungKembalian()` yang tidak tersedia dengan pengaksesan properti `kembalian`.


### 2. Penyaringan Omzet Harian
* Perhitungan pada `KafeService.hitungTotalOmsetHarian()` disarankan untuk menambahkan filter tanggal transaksi harian agar tidak bercampur apabila aplikasi berjalan melewati pergantian hari.

### 3. Status Pembayaran QRIS
* Fitur pembayaran QRIS pada antarmuka CLI dikonfirmasi berstatus sebagai simulasi (konfirmasi manual Y/N oleh kasir) dan belum terhubung ke *payment gateway* nyata.

### 4. Cakupan Pengujian Katalog (TC-01)
* Menyelaraskan dokumentasi README dengan cakupan aktual pada `KafeServiceTest.kt` yang saat ini berfokus memvalidasi ketersediaan menu per kategori.

### 5. Konsistensi Istilah Baku
* Menggunakan ejaan baku **"omzet"** secara konsisten di seluruh dokumen proyek menggantikan penulisan non-baku "omset".