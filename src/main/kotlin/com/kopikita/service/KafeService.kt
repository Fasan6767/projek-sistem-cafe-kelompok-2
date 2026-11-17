package com.kopikita.service

import com.kopikita.exception.MenuTidakDitemukanException
import com.kopikita.exception.StokTidakCukupException
import com.kopikita.model.*
import com.kopikita.payment.MetodePembayaran

/**
 * Service utama yang mengelola logika bisnis sistem KopiKita POS.
 *
 * Tanggung jawab:
 * - Manajemen katalog menu (tambah, cari, filter).
 * - Pembuatan pesanan baru dengan validasi stok otomatis (mencegah overselling).
 * - Pemrosesan transaksi keuangan secara polimorfik melalui [MetodePembayaran].
 * - Pencatatan riwayat transaksi sukses dan rekapitulasi omset harian.
 *
 * Penanggung jawab: Mochammad Faishal Ghani Iskandar (Business Logic Developer)
 */
class KafeService {

    // Menyimpan daftar katalog menu secara internal (Enkapsulasi)
    private val _daftarMenu: MutableList<MenuItem> = mutableListOf()

    // Getter publik bertipe List (read-only) untuk melindungi koleksi internal
    val daftarMenu: List<MenuItem>
        get() = _daftarMenu.toList()

    // Menyimpan riwayat pesanan yang telah berhasil diproses
    private val _riwayatPesanan: MutableList<Pesanan> = mutableListOf()

    val riwayatPesanan: List<Pesanan>
        get() = _riwayatPesanan.toList()

    private var counterIdPesanan: Int = 1

    init {
        inisialisasiKatalogBawaan()
    }

    /**
     * Memuat data awal (seeding) katalog menu kafe untuk Makanan, Minuman, dan Camilan.
     */
    private fun inisialisasiKatalogBawaan() {
        // Kategori: Makanan
        tambahMenu(Makanan("M01", "Nasi Goreng Spesial Kafe", 28000.0, 15, levelPedas = 2, isHangat = true))
        tambahMenu(Makanan("M02", "Mie Dok-Dok Pedas Manis", 22000.0, 20, levelPedas = 3, isHangat = true))
        tambahMenu(Makanan("M03", "Salad Sayur Saus Wijen", 25000.0, 10, levelPedas = 0, isHangat = false))

        // Kategori: Minuman
        tambahMenu(Minuman("D01", "Kopi Susu Gula Aren", 18000.0, 30, ukuran = "Reguler", suhu = "Dingin"))
        tambahMenu(Minuman("D02", "Americano Double Shot", 20000.0, 25, ukuran = "Large", suhu = "Panas"))
        tambahMenu(Minuman("D03", "Matcha Latte Creamy", 24000.0, 15, ukuran = "Reguler", suhu = "Dingin"))

        // Kategori: Camilan
        tambahMenu(Camilan("S01", "Croissant Butter Prancis", 22000.0, 12, porsi = "1 Pcs", isManis = true))
        tambahMenu(Camilan("S02", "French Fries Keju Mayo", 18000.0, 25, porsi = "Medium", isManis = false))
        tambahMenu(Camilan("S03", "Donat Coklat Karamel", 15000.0, 20, porsi = "2 Pcs", isManis = true))
    }

    /**
     * Menambahkan item menu baru ke dalam katalog.
     */
    fun tambahMenu(menu: MenuItem) {
        val sudahAda = _daftarMenu.any { it.kode.equals(menu.kode, ignoreCase = true) }
        require(!sudahAda) { "Menu dengan kode '${menu.kode}' sudah terdaftar dalam katalog." }
        _daftarMenu.add(menu)
    }

    /**
     * Mencari menu berdasarkan kode menu secara case-insensitive.
     * Mengembalikan objek MenuItem atau null jika tidak ditemukan.
     */
    fun cariMenuByKode(kode: String): MenuItem? {
        return _daftarMenu.firstOrNull { it.kode.equals(kode.trim(), ignoreCase = true) }
    }

    /**
     * Memfilter menu berdasarkan kategori (Makanan, Minuman, Camilan).
     */
    fun getMenuByKategori(kategori: String): List<MenuItem> {
        return _daftarMenu.filter { it.infoKategori().equals(kategori.trim(), ignoreCase = true) }
    }

    /**
     * Membuat objek [Pesanan] baru berdasarkan data keranjang belanja kasir.
     *
     * Validasi yang diterapkan:
     * - Nama pelanggan tidak boleh kosong.
     * - Keranjang tidak boleh kosong.
     * - Setiap kode menu harus terdaftar di katalog (jika tidak, melempar [MenuTidakDitemukanException]).
     * - Kuantitas menu harus lebih dari 0.
     * - Menu tidak boleh berstatus habis (stok = 0).
     * - Kuantitas pesanan tidak boleh melebihi sisa stok (jika melebihi, melempar [StokTidakCukupException]).
     */
    fun buatPesanan(namaPelanggan: String, keranjang: Map<String, Int>): Pesanan {
        require(namaPelanggan.isNotBlank()) { "Nama pelanggan tidak boleh kosong." }
        require(keranjang.isNotEmpty()) { "Keranjang pesanan tidak boleh kosong." }

        val idPesanan = "ORD-%03d".format(counterIdPesanan++)
        val pesanan = Pesanan(idPesanan, namaPelanggan.trim())

        for ((kode, kuantitas) in keranjang) {
            require(kuantitas > 0) { "Kuantitas untuk menu '$kode' harus lebih dari 0." }

            val menu = cariMenuByKode(kode)
                ?: throw MenuTidakDitemukanException("Menu dengan kode '$kode' tidak ditemukan di katalog.")

            if (menu.stok == 0) {
                throw StokTidakCukupException("Menu '${menu.nama}' ($kode) berstatus [HABIS].")
            }

            if (kuantitas > menu.stok) {
                throw StokTidakCukupException(
                    "Stok menu '${menu.nama}' ($kode) tidak mencukupi (tersedia: ${menu.stok}, diminta: $kuantitas)."
                )
            }

            pesanan.tambahItem(ItemPesanan(menu, kuantitas))
        }

        return pesanan
    }

    /**
     * Memproses transaksi pembayaran pesanan secara polimorfik.
     *
     * Logika transaksi:
     * 1. Memvalidasi ketersediaan item pesanan dan status transaksi sebelumnya.
     * 2. Melakukan validasi ulang ketersediaan stok inventaris sebelum pembayaran diproses.
     * 3. Menjalankan proses pembayaran via [MetodePembayaran.prosesPembayaran].
     * 4. Jika pembayaran berhasil:
     *    - Mengurangi stok setiap menu sesuai kuantitas pesanan.
     *    - Menandai status pesanan menjadi lunas.
     *    - Mencatat pesanan ke dalam riwayat transaksi harian.
     * 5. Jika pembayaran gagal:
     *    - Stok tidak berubah dan pesanan tidak masuk ke riwayat transaksi.
     *
     * @return true jika transaksi berhasil dan lunas, false jika gagal.
     */
    fun prosesTransaksi(pesanan: Pesanan, metode: MetodePembayaran): Boolean {
        if (pesanan.daftarItem.isEmpty() || pesanan.statusLunas) {
            return false
        }

        // Validasi ketersediaan stok untuk seluruh item
        val stokCukupSemua = pesanan.daftarItem.all { it.menu.stok >= it.kuantitas }
        if (!stokCukupSemua) {
            return false
        }

        val totalTagihan = pesanan.hitungTotal()
        val pembayaranSukses = metode.prosesPembayaran(totalTagihan)

        if (!pembayaranSukses) {
            return false
        }

        // Potong stok inventaris secara atomic setelah pembayaran terverifikasi
        for (item in pesanan.daftarItem) {
            item.menu.kurangiStok(item.kuantitas)
        }

        // Tandai pesanan sebagai lunas dan simpan ke riwayat transaksi
        pesanan.selesaikanPembayaran(metode)
        _riwayatPesanan.add(pesanan)

        return true
    }

    /**
     * Menghitung total omset harian dari seluruh transaksi yang telah berstatus lunas.
     */
    fun hitungTotalOmsetHarian(): Double {
        return _riwayatPesanan
            .filter { it.statusLunas }
            .sumOf { it.hitungTotal() }
    }
}
