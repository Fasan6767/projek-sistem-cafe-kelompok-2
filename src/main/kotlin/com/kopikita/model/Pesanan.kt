// Penanggung jawab: Syaroful Haidar Sudaryono | Domain Developer
package com.kopikita.model

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/** Status yang mungkin dimiliki sebuah pesanan. */
enum class StatusPesanan {
    MENUNGGU_PEMBAYARAN,
    LUNAS,
    DIBATALKAN
}

/**
 * Menyimpan item, perhitungan, dan status pesanan pelanggan.
 * Perubahan daftar item dan status dilakukan melalui method agar stok
 * serta aturan transaksi tetap konsisten.
 */
class Pesanan(
    val idPesanan: String,
    val namaPelanggan: String,
    val waktuDibuat: LocalDateTime = LocalDateTime.now()
) {
    private val daftarItemInternal = mutableListOf<ItemPesanan>()

    /** Snapshot read-only; pemanggil tidak dapat mengubah koleksi internal. */
    val items: List<ItemPesanan>
        get() = daftarItemInternal.toList()

    /** Alias nama properti sesuai diagram kelas. */
    val daftarItem: List<ItemPesanan>
        get() = items

    var status: StatusPesanan = StatusPesanan.MENUNGGU_PEMBAYARAN
        private set

    val statusLunas: Boolean
        get() = status == StatusPesanan.LUNAS

    init {
        require(idPesanan.isNotBlank()) { "ID pesanan tidak boleh kosong." }
        require(namaPelanggan.isNotBlank()) { "Nama pelanggan tidak boleh kosong." }
    }

    /** Menambahkan menu dan langsung mengurangi stok yang dipesan. */
    fun tambahItem(menu: MenuItem, kuantitas: Int) {
        pastikanMasihTerbuka()
        require(kuantitas > 0) { "Kuantitas harus lebih dari 0." }

        val itemLama = daftarItemInternal.firstOrNull { it.menuItem.id == menu.id }
        val kuantitasBaru = if (itemLama == null) {
            kuantitas
        } else {
            check(itemLama.menuItem === menu) {
                "ID menu ${menu.id} sudah digunakan oleh objek menu yang berbeda."
            }
            Math.addExact(itemLama.kuantitas, kuantitas)
        }

        // Validasi subtotal dilakukan sebelum stok dikurangi.
        val itemBaru = ItemPesanan(menu, kuantitasBaru)
        menu.kurangiStok(kuantitas)

        if (itemLama == null) {
            daftarItemInternal.add(itemBaru)
        } else {
            val index = daftarItemInternal.indexOf(itemLama)
            daftarItemInternal[index] = itemBaru
        }
    }

    /** Overload untuk menerima objek ItemPesanan secara langsung. */
    fun tambahItem(item: ItemPesanan) = tambahItem(item.menuItem, item.kuantitas)

    fun hitungTotal(): Double {
        val total = daftarItemInternal.sumOf { it.subtotal }
        check(total.isFinite()) { "Total pesanan berada di luar batas perhitungan." }
        return total
    }

    /** Pajak restoran ditetapkan sebesar 10% dari subtotal. */
    fun hitungPajak(): Double {
        val pajak = hitungTotal() * TARIF_PAJAK
        check(pajak.isFinite()) { "Nilai pajak berada di luar batas perhitungan." }
        return pajak
    }

    fun hitungGrandTotal(): Double {
        val totalAkhir = hitungTotal() + hitungPajak()
        check(totalAkhir.isFinite()) { "Total akhir pesanan berada di luar batas perhitungan." }
        return totalAkhir
    }

    /** Pesanan menjadi lunas hanya setelah service menerima pembayaran berhasil. */
    fun tandaiLunas() {
        pastikanMasihTerbuka()
        check(daftarItemInternal.isNotEmpty()) { "Pesanan kosong tidak dapat dilunasi." }
        status = StatusPesanan.LUNAS
    }

    /** Membatalkan pesanan dan mengembalikan stok setiap menu. */
    fun batalkan() {
        pastikanMasihTerbuka()

        // Validasi seluruh item lebih dahulu supaya tidak ada pengembalian parsial.
        daftarItemInternal.forEach { item ->
            check(item.menuItem.stok <= Int.MAX_VALUE - item.kuantitas) {
                "Stok ${item.menuItem.nama} tidak dapat dikembalikan karena melewati batas maksimum."
            }
        }
        daftarItemInternal.forEach { item -> item.menuItem.tambahStok(item.kuantitas) }
        status = StatusPesanan.DIBATALKAN
    }

    fun getFormattedWaktu(): String = waktuDibuat.format(FORMAT_WAKTU)

    private fun pastikanMasihTerbuka() {
        check(status == StatusPesanan.MENUNGGU_PEMBAYARAN) {
            "Pesanan dengan status $status tidak dapat diubah."
        }
    }

    private companion object {
        const val TARIF_PAJAK = 0.10
        val FORMAT_WAKTU: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
    }
}
