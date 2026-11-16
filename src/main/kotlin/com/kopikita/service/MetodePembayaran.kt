package com.kopikita.service

import com.kopikita.exception.PembayaranGagalException

/**
 * Data class hasil pembayaran
 */
data class HasilPembayaran(
    val sukses: Boolean,
    val metode: String,
    val totalTagihan: Double,
    val jumlahDibayar: Double,
    val kembalian: Double,
    val catatan: String = ""
)

/**
 * Interface untuk Polymorphic Payment Method
 */
interface MetodePembayaran {
    val namaMetode: String

    /**
     * Memproses pembayaran berdasarkan total tagihan dan uang/saldo yang diberikan
     * @throws PembayaranGagalException jika nominal bayar tidak mencukupi
     */
    fun prosesPembayaran(totalTagihan: Double, jumlahBayar: Double): HasilPembayaran
}

/**
 * Implementasi Pembayaran Tunai (Cash)
 */
class PembayaranTunai : MetodePembayaran {
    override val namaMetode: String = "Tunai (Cash)"

    override fun prosesPembayaran(totalTagihan: Double, jumlahBayar: Double): HasilPembayaran {
        if (jumlahBayar < totalTagihan) {
            val kekurangan = totalTagihan - jumlahBayar
            throw PembayaranGagalException("Uang tunai kurang sebesar Rp %,.0f.".format(kekurangan))
        }
        val kembalian = jumlahBayar - totalTagihan
        return HasilPembayaran(
            sukses = true,
            metode = namaMetode,
            totalTagihan = totalTagihan,
            jumlahDibayar = jumlahBayar,
            kembalian = kembalian,
            catatan = "Pembayaran tunai diterima dengan kembalian Rp %,.0f".format(kembalian)
        )
    }
}

/**
 * Implementasi Pembayaran Digital QRIS
 */
class PembayaranQris(val merchantNmid: String = "ID1020304050") : MetodePembayaran {
    override val namaMetode: String = "QRIS Digital"

    override fun prosesPembayaran(totalTagihan: Double, jumlahBayar: Double): HasilPembayaran {
        // QRIS harus pas (tanpa kembalian tunai)
        if (jumlahBayar < totalTagihan) {
            throw PembayaranGagalException("Saldo QRIS tidak mencukupi tagihan.")
        }
        return HasilPembayaran(
            sukses = true,
            metode = namaMetode,
            totalTagihan = totalTagihan,
            jumlahDibayar = totalTagihan,
            kembalian = 0.0,
            catatan = "Transaksi QRIS Sukses via Merchant NMID: $merchantNmid"
        )
    }
}
