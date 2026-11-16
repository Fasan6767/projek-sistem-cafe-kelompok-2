# 👥 Laporan Kontribusi Tim (CONTRIBUTORS.md)

**Mata Kuliah:** Pemrograman Berorientasi Objek (PBO)  
**Program Studi:** D4 Teknologi Rekayasa Informatika Industri (Polman Bandung)  
**Proyek:** KopiKita POS — Sistem Kasir Kafe Cerdas Berbasis OOP Kotlin  
**Semester:** Ganjil 2026/2027  

---

## Susunan Anggota & Pembagian Peran

| No | Nama Anggota | NIM | Peran Resmi | Tanggung Jawab Utama | Folder / Berkas yang Dikelola |
|---|---|---|---|---|---|
| 1 | **Fatih** | *[NIM Fatih]* | **Project Lead / Arsitek** | Koordinasi proyek, User Story, review Pull Request, dokumentasi utama | `README.md`, `CONTRIBUTORS.md`, `docs/` |
| 2 | **Haidar** | *[NIM Haidar]* | **Domain Developer** | Merancang model entitas OOP, abstract class `MenuItem`, 3 class turunan, enkapsulasi data | `src/main/kotlin/com/kopikita/model/` |
| 3 | **Faisal** | *[NIM Faisal]* | **Business Logic Developer** | Logika transaksi kafe, kalkulasi diskon & pajak, minimal 2 custom exception bisnis | `src/main/kotlin/com/kopikita/service/`, `exception/` |
| 4 | **Adil** | *[NIM Adil]* | **UI / CLI Developer** | Antarmuka CLI interaktif di terminal, validasi input bebas crash, rekaman video demo | `src/main/kotlin/com/kopikita/Main.kt` |
| 5 | **Awan** | *[NIM Awan]* | **QA + Data & Persistence** | Menulis unit test JUnit 5 (minimal 5 skenario), validasi koleksi data & persistence | `src/test/kotlin/com/kopikita/` |
| 6 | **Febri** | *[NIM Febri]* | **DevOps** | Konfigurasi Gradle, Dockerfile, docker-compose, CI otomatis GitHub Actions | `build.gradle.kts`, `Dockerfile`, `docker-compose.yml`, `.github/` |

---

## Log Kontribusi Harian

* **Hari 1 (Senin):**
  - Fatih & Tim: Brainstorming tema & perumusan 5 User Story utama.
  - Fatih & Haidar: Perancangan Class Diagram UML (Mermaid).
  - Febri: Inisialisasi struktur proyek Gradle, Dockerfile, docker-compose, dan GitHub Actions CI.
* **Hari 2 (Selasa - Rabu):**
  - Haidar: Implementasi package `model/` (`MenuItem`, `Makanan`, `Minuman`, `Camilan`, `Pesanan`).
  - Faisal: Implementasi package `service/` (`KafeService`, `MetodePembayaran`) dan `exception/`.
  - Adil: Implementasi menu CLI dan cetak struk pembayaran di `Main.kt`.
  - Awan: Implementasi test suite JUnit 5 di `src/test/kotlin/` dengan 7 skenario pengujian.
* **Hari 3 (Kamis):**
  - Tim: Integrasi, review Pull Request, perbaikan bug, dan pengujian build Docker.
  - Adil: Perekaman video demonstrasi aplikasi ($\le$ 5 menit).
  - Fatih: Finalisasi laporan dan pengumpulan tautan repositori GitHub ke LMS.
