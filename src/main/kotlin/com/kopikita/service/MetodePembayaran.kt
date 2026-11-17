// Penanggung jawab: Syaroful Haidar Sudaryono | Domain Developer
package com.kopikita.service

import com.kopikita.exception.PembayaranGagalException
import java.util.UUID

/** Ringkasan hasil transaksi yang ditampilkan pada struk. */
data class HasilPembayaran(
    val sukses: Boolean,
    val metode: String,
    val totalTagihan: Double,
    val jumlahDibayar: Double,
    val kembalian: Double,
    val catatan: String = ""
)

/** Kontrak umum untuk semua metode pembayaran. */
interface MetodePembayaran {
    val namaMetode: String

    /**
     * Memproses pembayaran terhadap total tagihan.
     * Implementasi melempar PembayaranGagalException bila nominal tidak sesuai.
     */
    fun prosesPembayaran(totalTagihan: Double, jumlahBayar: Double): HasilPembayaran

    /** Menghasilkan detail pembayaran terakhir untuk ditampilkan pada struk. */
    fun getDetailStruk(): String
}

/** Pembayaran tunai dengan validasi nominal dan perhitungan kembalian. */
class PembayaranTunai : MetodePembayaran {
    override val namaMetode: String = "Tunai (Cash)"

    private var hasilTerakhir: HasilPembayaran? = null

    val uangDibayar: Double
        get() = hasilTerakhir?.jumlahDibayar ?: 0.0

    val kembalian: Double
        get() = hasilTerakhir?.kembalian ?: 0.0

    override fun prosesPembayaran(totalTagihan: Double, jumlahBayar: Double): HasilPembayaran {
        validasiNominal(totalTagihan, jumlahBayar)
        hasilTerakhir = null

        if (jumlahBayar + TOLERANSI < totalTagihan) {
            val kekurangan = totalTagihan - jumlahBayar
            throw PembayaranGagalException(
                "Uang tunai kurang sebesar Rp %,.0f.".format(kekurangan)
            )
        }

        val hasil = HasilPembayaran(
            sukses = true,
            metode = namaMetode,
            totalTagihan = totalTagihan,
            jumlahDibayar = jumlahBayar,
            kembalian = (jumlahBayar - totalTagihan).coerceAtLeast(0.0),
            catatan = "Pembayaran tunai diterima. Kembalian Rp %,.0f.".format(
                (jumlahBayar - totalTagihan).coerceAtLeast(0.0)
            )
        )
        hasilTerakhir = hasil
        return hasil
    }

    override fun getDetailStruk(): String =
        hasilTerakhir?.catatan ?: "Pembayaran tunai belum diproses."
}

/** Pembayaran QRIS simulasi dengan nominal harus sama dengan tagihan. */
class PembayaranQris(
    val merchantNmid: String = "ID1020304050"
) : MetodePembayaran {
    override val namaMetode: String = "QRIS Digital"

    /** Kode referensi dibuat setiap kali pembayaran QRIS berhasil. */
    var kodeReferensi: String = ""
        private set

    private var hasilTerakhir: HasilPembayaran? = null

    override fun prosesPembayaran(totalTagihan: Double, jumlahBayar: Double): HasilPembayaran {
        validasiNominal(totalTagihan, jumlahBayar)
        hasilTerakhir = null

        if (kotlin.math.abs(jumlahBayar - totalTagihan) > TOLERANSI) {
            val pesan = if (jumlahBayar < totalTagihan) {
                "Nominal QRIS kurang sebesar Rp %,.0f.".format(totalTagihan - jumlahBayar)
            } else {
                "Nominal QRIS harus sama persis dengan total tagihan."
            }
            throw PembayaranGagalException(pesan)
        }

        kodeReferensi = "QR-${UUID.randomUUID().toString().take(8).uppercase()}"
        val hasil = HasilPembayaran(
            sukses = true,
            metode = namaMetode,
            totalTagihan = totalTagihan,
            jumlahDibayar = totalTagihan,
            kembalian = 0.0,
            catatan = "QRIS berhasil diverifikasi | NMID: $merchantNmid | Ref: $kodeReferensi"
        )
        hasilTerakhir = hasil
        return hasil
    }

    override fun getDetailStruk(): String =
        hasilTerakhir?.catatan ?: "Pembayaran QRIS belum diproses."

    companion object {
        private const val TOLERANSI = 0.001
    }
}

/** Alias nama QRIS kapital agar sesuai penamaan pada diagram dan dokumentasi. */
typealias PembayaranQRIS = PembayaranQris

private const val TOLERANSI = 0.001

private fun validasiNominal(totalTagihan: Double, jumlahBayar: Double) {
    require(totalTagihan.isFinite() && totalTagihan >= 0.0) {
        "Total tagihan harus berupa angka valid dan tidak negatif."
    }
    require(jumlahBayar.isFinite() && jumlahBayar >= 0.0) {
        "Jumlah pembayaran harus berupa angka valid dan tidak negatif."
    }
}
