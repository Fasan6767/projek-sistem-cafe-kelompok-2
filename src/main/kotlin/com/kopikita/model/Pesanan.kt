// Penanggung jawab: Haidar | Domain Developer
package com.kopikita.model

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/** Status yang menggambarkan tahapan pembayaran pesanan. */
enum class StatusPesanan {
    MENUNGGU_PEMBAYARAN,
    LUNAS,
    DIBATALKAN
}

/**
 * Menyimpan data transaksi pelanggan.
 * Daftar item dan status dikelola melalui method agar perubahan pesanan
 * tetap mengikuti aturan stok dan siklus pembayaran.
 *
 * Penanggung jawab bagian Domain Model: Haidar
 */
class Pesanan(
    val idPesanan: String,
    val namaPelanggan: String,
    val waktuDibuat: LocalDateTime = LocalDateTime.now()
) {

    private val daftarItem = mutableListOf<ItemPesanan>()

    /** Salinan read-only agar pemanggil tidak dapat mengubah daftar internal. */
    val items: List<ItemPesanan>
        get() = daftarItem.toList()

    var status: StatusPesanan = StatusPesanan.MENUNGGU_PEMBAYARAN
        private set

    init {
        require(idPesanan.isNotBlank()) { "ID pesanan tidak boleh kosong." }
        require(namaPelanggan.isNotBlank()) { "Nama pelanggan tidak boleh kosong." }
    }

    /** Menambahkan menu ke pesanan sekaligus mengurangi persediaannya. */
    fun tambahItem(item: MenuItem, kuantitas: Int) {
        pastikanMasihDapatDiubah()
        require(kuantitas > 0) { "Kuantitas harus lebih dari 0." }

        val itemTersimpan = daftarItem.firstOrNull { it.menuItem.id == item.id }
        val kuantitasBaru = if (itemTersimpan != null) {
            check(itemTersimpan.menuItem === item) {
                "ID menu ${item.id} sudah digunakan oleh objek menu yang berbeda."
            }
            Math.addExact(itemTersimpan.kuantitas, kuantitas)
        } else {
            kuantitas
        }

        // Bentuk objek lebih dahulu agar validasi subtotal dilakukan sebelum stok berubah.
        val itemBaru = ItemPesanan(item, kuantitasBaru)
        item.kurangiStok(kuantitas)

        if (itemTersimpan == null) {
            daftarItem.add(itemBaru)
        } else {
            val posisi = daftarItem.indexOf(itemTersimpan)
            daftarItem[posisi] = itemBaru
        }
    }

    /** Menghitung total seluruh item sebelum pajak. */
    fun hitungTotal(): Double {
        val total = daftarItem.sumOf { it.subtotal }
        check(total.isFinite()) { "Total pesanan berada di luar batas perhitungan." }
        return total
    }

    /** Menghitung pajak restoran sebesar 10 persen. */
    fun hitungPajak(): Double {
        val pajak = hitungTotal() * TARIF_PAJAK
        check(pajak.isFinite()) { "Nilai pajak berada di luar batas perhitungan." }
        return pajak
    }

    /** Menghitung jumlah akhir yang harus dibayar pelanggan. */
    fun hitungGrandTotal(): Double {
        val grandTotal = hitungTotal() + hitungPajak()
        check(grandTotal.isFinite()) { "Total akhir pesanan berada di luar batas perhitungan." }
        return grandTotal
    }

    /** Mengubah status pesanan menjadi lunas setelah pembayaran berhasil. */
    fun tandaiLunas() {
        pastikanMasihDapatDiubah()
        check(daftarItem.isNotEmpty()) { "Pesanan kosong tidak dapat dilunasi." }
        status = StatusPesanan.LUNAS
    }

    /**
     * Membatalkan pesanan dan mengembalikan stok yang sebelumnya dikurangi.
     * Pemeriksaan stok dilakukan lebih dahulu agar pengembalian tidak berhenti di tengah.
     */
    fun batalkan() {
        pastikanMasihDapatDiubah()

        daftarItem.forEach { item ->
            check(item.menuItem.stok <= Int.MAX_VALUE - item.kuantitas) {
                "Stok ${item.menuItem.nama} tidak dapat dikembalikan karena melewati batas maksimum."
            }
        }

        daftarItem.forEach { item ->
            item.menuItem.tambahStok(item.kuantitas)
        }
        status = StatusPesanan.DIBATALKAN
    }

    /** Mengubah waktu pembuatan menjadi format yang mudah dibaca. */
    fun getFormattedWaktu(): String = waktuDibuat.format(FORMAT_WAKTU)

    private fun pastikanMasihDapatDiubah() {
        check(status == StatusPesanan.MENUNGGU_PEMBAYARAN) {
            "Pesanan dengan status $status tidak dapat diubah."
        }
    }

    private companion object {
        const val TARIF_PAJAK = 0.10
        val FORMAT_WAKTU: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
    }
}
