package com.kopikita.model

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Status siklus hidup pesanan
 */
enum class StatusPesanan {
    MENUNGGU_PEMBAYARAN,
    LUNAS,
    DIBATALKAN
}

/**
 * Kelas Pesanan dengan Enkapsulasi Penuh:
 * - Daftar item pesanan disimpan secara private dan hanya bisa diakses sebagai immutable list.
 * - Status pesanan hanya dapat diubah melalui metode siklus hidup (tandaiLunas / batalkan).
 */
class Pesanan(
    val idPesanan: String,
    val namaPelanggan: String,
    val waktuDibuat: LocalDateTime = LocalDateTime.now()
) {
    private val _items = mutableListOf<ItemPesanan>()

    /**
     * Public getter untuk items yang mengembalikan list read-only.
     * Mencegah modifikasi langsung dari luar kelas.
     */
    val items: List<ItemPesanan>
        get() = _items.toList()

    var status: StatusPesanan = StatusPesanan.MENUNGGU_PEMBAYARAN
        private set

    /**
     * Menambahkan item pesanan
     */
    fun tambahItem(item: MenuItem, kuantitas: Int) {
        check(status == StatusPesanan.MENUNGGU_PEMBAYARAN) { "Pesanan sudah berstatus $status, tidak bisa diubah." }
        require(kuantitas > 0) { "Kuantitas harus lebih dari 0." }
        
        // Kurangi stok dari menu terkait
        item.kurangiStok(kuantitas)
        
        // Cek apakah item sudah ada di keranjang, jika ada tambahkan kuantitasnya
        val existingIndex = _items.indexOfFirst { it.menuItem.id == item.id }
        if (existingIndex != -1) {
            val existing = _items[existingIndex]
            _items[existingIndex] = existing.copy(kuantitas = existing.kuantitas + kuantitas)
        } else {
            _items.add(ItemPesanan(item, kuantitas))
        }
    }

    /**
     * Menghitung total harga sebelum pajak
     */
    fun hitungTotal(): Double {
        return _items.sumOf { it.subtotal }
    }

    /**
     * Pajak resto 10%
     */
    fun hitungPajak(): Double {
        return hitungTotal() * 0.10
    }

    /**
     * Total biaya akhir yang harus dibayarkan pelanggan
     */
    fun hitungGrandTotal(): Double {
        return hitungTotal() + hitungPajak()
    }

    /**
     * Menyelesaikan status pesanan menjadi Lunas
     */
    fun tandaiLunas() {
        check(status == StatusPesanan.MENUNGGU_PEMBAYARAN) { "Pesanan tidak bisa dilunasi karena statusnya $status." }
        status = StatusPesanan.LUNAS
    }

    /**
     * Membatalkan pesanan dan mengembalikan stok menu
     */
    fun batalkan() {
        check(status == StatusPesanan.MENUNGGU_PEMBAYARAN) { "Pesanan dengan status $status tidak dapat dibatalkan." }
        // Kembalikan stok item
        for (item in _items) {
            item.menuItem.tambahStok(item.kuantitas)
        }
        status = StatusPesanan.DIBATALKAN
    }

    fun getFormattedWaktu(): String {
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
        return waktuDibuat.format(formatter)
    }
}
