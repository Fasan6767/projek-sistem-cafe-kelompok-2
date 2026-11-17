// Penanggung jawab: Syaroful Haidar Sudaryono | Domain Developer
package com.kopikita.model

/** Pilihan ukuran minuman dan tambahan harga yang dikenakan. */
enum class UkuranMinuman(val label: String, val tambahanHarga: Double) {
    REGULER("Reguler", 0.0),
    LARGE("Large", 4000.0)
}

/** Model produk minuman dengan pilihan ukuran serta suhu penyajian. */
class Minuman(
    id: String,
    nama: String,
    hargaDasar: Double,
    stokAwal: Int,
    val ukuran: UkuranMinuman = UkuranMinuman.REGULER,
    val isDingin: Boolean = true
) : MenuItem(id, nama, hitungHargaAkhir(hargaDasar, ukuran), stokAwal) {

    /** Konstruktor kompatibilitas untuk service yang memakai ukuran dan suhu String. */
    constructor(
        id: String,
        nama: String,
        hargaDasar: Double,
        stokAwal: Int,
        ukuran: String,
        suhu: String
    ) : this(
        id,
        nama,
        hargaDasar,
        stokAwal,
        parseUkuran(ukuran),
        parseSuhu(suhu)
    )

    override val kategori: String = "Minuman"

    override fun getDetail(): String {
        val suhu = if (isDingin) "Dingin" else "Panas"
        return "${ukuran.label} | $suhu"
    }

    companion object {
        private fun parseUkuran(ukuran: String): UkuranMinuman = when (ukuran.trim().lowercase()) {
            "reguler", "regular" -> UkuranMinuman.REGULER
            "large" -> UkuranMinuman.LARGE
            else -> throw IllegalArgumentException("Ukuran minuman '$ukuran' tidak dikenal.")
        }

        private fun parseSuhu(suhu: String): Boolean = when (suhu.trim().lowercase()) {
            "dingin", "cold" -> true
            "panas", "hot" -> false
            else -> throw IllegalArgumentException("Suhu minuman '$suhu' tidak dikenal.")
        }

        private fun hitungHargaAkhir(hargaDasar: Double, ukuran: UkuranMinuman): Double {
            require(hargaDasar.isFinite() && hargaDasar >= 0.0) {
                "Harga dasar minuman harus berupa angka valid dan tidak negatif."
            }
            val hargaAkhir = hargaDasar + ukuran.tambahanHarga
            require(hargaAkhir.isFinite()) { "Harga akhir minuman berada di luar batas perhitungan." }
            return hargaAkhir
        }
    }
}
