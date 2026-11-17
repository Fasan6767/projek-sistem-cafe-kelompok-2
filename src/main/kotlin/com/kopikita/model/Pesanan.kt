// Penanggung jawab: Syaroful Haidar Sudaryono | Domain Developer
package com.kopikita.model

import com.kopikita.payment.MetodePembayaran
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
 * Stok baru dikurangi oleh KafeService setelah pembayaran berhasil,
 * sehingga stok tidak berubah bila pembayaran gagal atau pesanan dibatalkan.
 */
class Pesanan(
    val idPesanan: String,
    val namaPelanggan: String,
    val waktuDibuat: LocalDateTime = LocalDateTime.now()
) {
    private val daftarItemInternal = mutableListOf<ItemPesanan>()

    val items: List<ItemPesanan>
        get() = daftarItemInternal.toList()

    val daftarItem: List<ItemPesanan>
        get() = items

    var status: StatusPesanan = StatusPesanan.MENUNGGU_PEMBAYARAN
        private set

    val statusLunas: Boolean
        get() = status == StatusPesanan.LUNAS

    var metodePembayaran: MetodePembayaran? = null
        private set

    init {
        require(idPesanan.isNotBlank()) { "ID pesanan tidak boleh kosong." }
        require(namaPelanggan.isNotBlank()) { "Nama pelanggan tidak boleh kosong." }
    }

    /** Menambahkan item setelah memeriksa stok, tanpa memotong stok inventaris. */
    fun tambahItem(menu: MenuItem, kuantitas: Int) {
        pastikanMasihTerbuka()
        require(kuantitas > 0) { "Kuantitas harus lebih dari 0." }

        val itemLama = daftarItemInternal.firstOrNull { it.menu.id == menu.id }
        val kuantitasBaru = if (itemLama == null) {
            kuantitas
        } else {
            check(itemLama.menu === menu) {
                "ID menu ${menu.id} sudah digunakan oleh objek menu yang berbeda."
            }
            Math.addExact(itemLama.kuantitas, kuantitas)
        }

        check(menu.stokMencukupi(kuantitasBaru)) {
            "Stok ${menu.nama} tidak mencukupi. Tersedia: ${menu.stok}, diminta: $kuantitasBaru."
        }

        val itemBaru = ItemPesanan(menu, kuantitasBaru)
        if (itemLama == null) {
            daftarItemInternal.add(itemBaru)
        } else {
            val index = daftarItemInternal.indexOf(itemLama)
            daftarItemInternal[index] = itemBaru
        }
    }

    fun tambahItem(item: ItemPesanan) = tambahItem(item.menu, item.kuantitas)

    fun hitungTotal(): Double {
        val total = daftarItemInternal.sumOf { it.subtotal }
        check(total.isFinite()) { "Total pesanan berada di luar batas perhitungan." }
        return total
    }

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

    fun tandaiLunas() {
        pastikanMasihTerbuka()
        check(daftarItemInternal.isNotEmpty()) { "Pesanan kosong tidak dapat dilunasi." }
        status = StatusPesanan.LUNAS
    }

    /** Dipakai KafeService setelah pembayaran berhasil. */
    fun selesaikanPembayaran(metode: MetodePembayaran) {
        tandaiLunas()
        metodePembayaran = metode
    }

    /** Membatalkan pesanan; stok tidak perlu dikembalikan karena belum dipotong. */
    fun batalkan() {
        pastikanMasihTerbuka()
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
