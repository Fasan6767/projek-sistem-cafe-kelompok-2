package com.kopikita.service

import com.kopikita.exception.MenuTidakDitemukanException
import com.kopikita.exception.PembayaranGagalException
import com.kopikita.exception.StokTidakCukupException
import com.kopikita.model.*

/**
 * Service utama untuk mengelola alur bisnis Kafe.
 * Mengelola koleksi menu, pesanan, dan transaksi keuangan.
 */
class KafeService {

    // Menggunakan Map untuk pencarian O(1) berdasarkan ID Menu
    private val katalogMenu: MutableMap<String, MenuItem> = mutableMapOf()

    // Menggunakan List untuk riwayat pesanan kronologis
    private val daftarPesanan: MutableList<Pesanan> = mutableListOf()

    private var autoIncrementId: Int = 1

    init {
        // Inisialisasi data menu bawaan
        tambahMenu(Makanan("M01", "Nasi Goreng Spesial Kafe", 28000.0, 15, tingkatPedas = 2, isVegetarian = false))
        tambahMenu(Makanan("M02", "Mie Dok-Dok Pedas Manis", 22000.0, 20, tingkatPedas = 3, isVegetarian = false))
        tambahMenu(Makanan("M03", "Salad Sayur Saus Wijen", 25000.0, 10, tingkatPedas = 0, isVegetarian = true))

        tambahMenu(Minuman("D01", "Kopi Susu Gula Aren", 18000.0, 30, UkuranMinuman.REGULER, isDingin = true))
        tambahMenu(Minuman("D02", "Americano Double Shot", 20000.0, 25, UkuranMinuman.LARGE, isDingin = false))
        tambahMenu(Minuman("D03", "Matcha Latte Creamy", 24000.0, 15, UkuranMinuman.REGULER, isDingin = true))

        tambahMenu(Camilan("S01", "Croissant Butter Prancis", 22000.0, 12, porsi = "1 Pcs", isManis = true))
        tambahMenu(Camilan("S02", "French Fries Keju Mayo", 18000.0, 25, porsi = "Medium", isManis = false))
        tambahMenu(Camilan("S03", "Donat Coklat Karamel", 15000.0, 20, porsi = "2 Pcs", isManis = true))
    }

    /**
     * Menambahkan item menu baru ke katalog
     */
    fun tambahMenu(menu: MenuItem) {
        katalogMenu[menu.id] = menu
    }

    /**
     * Mendapatkan semua daftar menu
     */
    fun getSemuaMenu(): List<MenuItem> {
        return katalogMenu.values.toList()
    }

    /**
     * Mencari menu berdasarkan ID
     * @throws MenuTidakDitemukanException jika menu tidak ada
     */
    fun getMenuById(id: String): MenuItem {
        return katalogMenu[id] ?: throw MenuTidakDitemukanException(id)
    }

    /**
     * Filter menu berdasarkan kategori (Makanan, Minuman, Camilan)
     */
    fun getMenuByKategori(kategori: String): List<MenuItem> {
        return katalogMenu.values.filter { it.kategori.equals(kategori, ignoreCase = true) }
    }

    /**
     * Mencari menu berdasarkan kata kunci nama
     */
    fun cariMenu(keyword: String): List<MenuItem> {
        return katalogMenu.values.filter { it.nama.contains(keyword, ignoreCase = true) }
    }

    /**
     * Membuat pesanan baru
     */
    fun buatPesanan(namaPelanggan: String): Pesanan {
        val id = "ORD-%03d".format(autoIncrementId++)
        val pesanan = Pesanan(id, namaPelanggan)
        daftarPesanan.add(pesanan)
        return pesanan
    }

    /**
     * Menambahkan item ke dalam pesanan dengan validasi stok
     */
    fun tambahItemKePesanan(pesanan: Pesanan, menuId: String, kuantitas: Int) {
        val menu = getMenuById(menuId)
        if (menu.stok < kuantitas) {
            throw StokTidakCukupException(menu.nama, menu.stok, kuantitas)
        }
        pesanan.tambahItem(menu, kuantitas)
    }

    /**
     * Menyelesaikan pembayaran pesanan
     */
    fun bayarPesanan(
        pesanan: Pesanan,
        metode: MetodePembayaran,
        jumlahBayar: Double
    ): HasilPembayaran {
        if (pesanan.status == StatusPesanan.LUNAS) {
            throw PembayaranGagalException("Pesanan ${pesanan.idPesanan} sudah lunas.")
        }
        if (pesanan.items.isEmpty()) {
            throw PembayaranGagalException("Pesanan belum memiliki item yang dipesan.")
        }

        val grandTotal = pesanan.hitungGrandTotal()
        val hasil = metode.prosesPembayaran(grandTotal, jumlahBayar)
        if (hasil.sukses) {
            pesanan.tandaiLunas()
        }
        return hasil
    }

    /**
     * Mendapatkan semua riwayat pesanan
     */
    fun getSemuaPesanan(): List<Pesanan> {
        return daftarPesanan.toList()
    }

    /**
     * Ringkasan pendapatan total dari pesanan yang berstatus Lunas
     */
    fun hitungTotalPendapatan(): Double {
        return daftarPesanan
            .filter { it.status == StatusPesanan.LUNAS }
            .sumOf { it.hitungGrandTotal() }
    }
}
