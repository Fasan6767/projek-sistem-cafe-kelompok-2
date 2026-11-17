# 👥 Log Kontribusi Tim — KopiKita POS

Dokumen ini mencatat pembagian tanggung jawab teknis, alur kerja kolaborasi Git, serta log kontribusi dari seluruh anggota tim pengembang **KopiKita POS**.

---

## 1. Aturan Kolaborasi & Alur Git (Git Workflow)

Untuk menjaga kerapian kode selama pengembangan, seluruh anggota mengikuti konvensi berikut:
* **`main`**: Branch stabil siap rilis dan siap dipresentasikan ke dosen.
* **`dev`**: Branch integrasi utama tempat penggabungan seluruh fitur sebelum masuk ke `main`.
* **`feature/<nama-fitur>`**: Branch pengembangan fitur masing-masing anggota (contoh: `feature/domain-models`, `feature/kafe-service`, `feature/cli-menu`, `feature/unit-tests`, `feature/docker-setup`).
* **Format Pesan Commit**:
  * `feat: menambahkan kelas domain MenuItem dan turunannya`
  * `fix: memperbaiki validasi stok pada KafeService`
  * `test: menambahkan skenario unit test pembayaran tunai dan QRIS`
  * `docs: memperbarui README dan diagram UML`
  * `chore: menambahkan konfigurasi Dockerfile dan docker-compose`

---

## 2. Matriks Pembagian Tugas & Log Kontribusi

| No | Anggota (NIM) | Peran | Tanggung Jawab & Deliverables Utama | Status |
| :---: | :--- | :--- | :--- | :---: |
| 1 | **Fatih Ahsan**<br>(`225443033`) | **Project Lead / Arsitek** | • Merancang arsitektur OOP, diagram kelas UML, dan kontrak antar-layer.<br>• Menyusun dokumen `README.md` & `CONTRIBUTORS.md`.<br>• Melakukan *code review* dan *merge Pull Request* ke branch `dev` dan `main`. | 🟢 Selesai (Arsitektur & Docs) |
| 2 | **Syaroful Haidar Sudaryono**<br>(`225443051`) | **Domain Developer** | • Mengimplementasikan hierarki kelas `MenuItem` (`abstract`), `Makanan`, `Minuman`, dan `Camilan`.<br>• Mengimplementasikan model `ItemPesanan`, `Pesanan`, serta interface `MetodePembayaran` (`PembayaranTunai`, `PembayaranQRIS`). | 🟡 Dalam Pengerjaan |
| 3 | **Mochammad Faishal Ghani Iskandar**<br>(`225443040`) | **Business Logic Developer** | • Mengimplementasikan kelas `KafeService` untuk manajemen katalog menu, pembuatan pesanan, validasi stok otomatis, pemrosesan transaksi, dan kalkulasi omset harian. | 🟡 Dalam Pengerjaan |
| 4 | **Muhammad Adil Abdirrahim**<br>(`225443042`) | **UI / CLI Developer** | • Membangun antarmuka pengguna berbasis konsol (`KopiKitaCLI.kt` & `Main.kt`).<br>• Menangani navigasi menu kasir, input pesanan interaktif, format tampilan struk, dan penanganan kesalahan input pengguna. | 🟡 Dalam Pengerjaan |
| 5 | **Nasywan Aziz**<br>(`225443048`) | **QA + Data & Persistence** | • Menyediakan data awal (*seeding*) katalog menu kafe.<br>• Menyusun 8 skenario *unit test* otomatis menggunakan JUnit 5 (`KafeServiceTest.kt`) dan memastikan seluruh tes lulus (`.\gradlew.bat test`). | 🟡 Dalam Pengerjaan |
| 6 | **Febriansyah**<br>(`225443035`) | **DevOps** | • Mengonfigurasi proyek Gradle Kotlin (`build.gradle.kts`).<br>• Menyusun `Dockerfile` (multi-stage build) dan `docker-compose.yml` agar aplikasi dapat dijalankan dengan `docker compose run --rm kopikita-app`. | 🟡 Dalam Pengerjaan |

---

## 3. Catatan Log Aktivitas Harian

| Tanggal | Anggota | Ringkasan Aktivitas / Commit |
| :--- | :--- | :--- |
| 17 Nov 2026 | **Fatih Ahsan** | Inisialisasi cetak biru arsitektur kelas UML, penyusunan `README.md`, user stories, dan `CONTRIBUTORS.md`. |
| — | **Syaroful Haidar Sudaryono** | *(Isi setelah commit implementasi package `domain` dan `payment`)* |
| — | **Mochammad Faishal Ghani Iskandar** | *(Isi setelah commit implementasi `KafeService.kt`)* |
| — | **Muhammad Adil Abdirrahim** | *(Isi setelah commit implementasi `KopiKitaCLI.kt` dan `Main.kt`)* |
| — | **Nasywan Aziz** | *(Isi setelah commit data awal menu dan `KafeServiceTest.kt`)* |
| — | **Febriansyah** | *(Isi setelah commit `build.gradle.kts`, `Dockerfile`, dan `docker-compose.yml`)* |