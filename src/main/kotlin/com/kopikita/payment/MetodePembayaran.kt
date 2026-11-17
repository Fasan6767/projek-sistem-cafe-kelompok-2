// Penanggung jawab: Syaroful Haidar Sudaryono | Domain Developer
package com.kopikita.payment

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

/** Kontrak pembayaran yang kompatibel dengan KafeService milik Faishal. */
interface MetodePembayaran {
    val namaMetode: String

    /** Dipakai KafeService; true bila pembayaran berhasil, false bila gagal. */
    fun prosesPembayaran(totalTagihan: Double): Boolean

    /** Operasi terperinci untuk menghitung hasil pembayaran. */
    fun prosesPembayaran(totalTagihan: Double, jumlahBayar: Double): HasilPembayaran

    fun getDetailStruk(): String
}

/** Pembayaran tunai dengan nominal yang dimasukkan kasir melalui UI. */
class PembayaranTunai(
    val uangDibayar: Double
) : MetodePembayaran {
    override val namaMetode: String = "Tunai (Cash)"

    private var hasilTerakhir: HasilPembayaran? = null

    val kembalian: Double
        get() = hasilTerakhir?.kembalian ?: 0.0

    override fun prosesPembayaran(totalTagihan: Double): Boolean = try {
        prosesPembayaran(totalTagihan, uangDibayar)
        true
    } catch (_: PembayaranGagalException) {
        hasilTerakhir = null
        false
    }

    override fun prosesPembayaran(totalTagihan: Double, jumlahBayar: Double): HasilPembayaran {
        validasiNominal(totalTagihan, jumlahBayar)
        hasilTerakhir = null

        if (jumlahBayar + TOLERANSI < totalTagihan) {
            throw PembayaranGagalException(
                "Uang tunai kurang sebesar Rp %,.0f.".format(totalTagihan - jumlahBayar)
            )
        }

        return HasilPembayaran(
            sukses = true,
            metode = namaMetode,
            totalTagihan = totalTagihan,
            jumlahDibayar = jumlahBayar,
            kembalian = (jumlahBayar - totalTagihan).coerceAtLeast(0.0),
            catatan = "Pembayaran tunai diterima. Kembalian Rp %,.0f.".format(
                (jumlahBayar - totalTagihan).coerceAtLeast(0.0)
            )
        ).also { hasilTerakhir = it }
    }

    override fun getDetailStruk(): String =
        hasilTerakhir?.catatan ?: "Pembayaran tunai belum diproses."
}

/** Pembayaran QRIS simulasi; kode referensi tersedia saat objek dibuat untuk ditampilkan UI. */
class PembayaranQRIS(
    val merchantNmid: String = "ID1020304050"
) : MetodePembayaran {
    override val namaMetode: String = "QRIS Digital"

    val kodeReferensi: String = "QR-${UUID.randomUUID().toString().take(8).uppercase()}"

    private var hasilTerakhir: HasilPembayaran? = null

    override fun prosesPembayaran(totalTagihan: Double): Boolean = try {
        prosesPembayaran(totalTagihan, totalTagihan)
        true
    } catch (_: PembayaranGagalException) {
        hasilTerakhir = null
        false
    }

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

        return HasilPembayaran(
            sukses = true,
            metode = namaMetode,
            totalTagihan = totalTagihan,
            jumlahDibayar = totalTagihan,
            kembalian = 0.0,
            catatan = "QRIS berhasil diverifikasi | NMID: $merchantNmid | Ref: $kodeReferensi"
        ).also { hasilTerakhir = it }
    }

    override fun getDetailStruk(): String =
        hasilTerakhir?.catatan ?: "Pembayaran QRIS belum diproses."
}

private const val TOLERANSI = 0.001

private fun validasiNominal(totalTagihan: Double, jumlahBayar: Double) {
    require(totalTagihan.isFinite() && totalTagihan >= 0.0) {
        "Total tagihan harus berupa angka valid dan tidak negatif."
    }
    require(jumlahBayar.isFinite() && jumlahBayar >= 0.0) {
        "Jumlah pembayaran harus berupa angka valid dan tidak negatif."
    }
}
