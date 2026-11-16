// Penanggung jawab: Haidar | Domain Developer
package com.kopikita.model

/** Pilihan ukuran minuman beserta tambahan harga untuk setiap ukuran. */
enum class UkuranMinuman(val label: String, val tambahanHarga: Double) {
    REGULER("Reguler", 0.0),
    LARGE("Large", 4000.0);

    init {
        require(tambahanHarga.isFinite() && tambahanHarga >= 0.0) {
            "Tambahan harga ukuran minuman tidak boleh negatif."
        }
    }
}

/**
 * Model menu minuman dengan pilihan ukuran dan penyajian dingin atau panas.
 * Harga yang disimpan pada MenuItem adalah harga dasar ditambah biaya ukuran.
 *
 * Penanggung jawab bagian Domain Model: Haidar
 */
class Minuman(
    id: String,
    nama: String,
    hargaDasar: Double,
    stokAwal: Int,
    val ukuran: UkuranMinuman = UkuranMinuman.REGULER,
    val isDingin: Boolean = true
) : MenuItem(id, nama, hargaDasar + ukuran.tambahanHarga, stokAwal) {

    override val kategori: String = "Minuman"

    init {
        require(hargaDasar.isFinite() && hargaDasar >= 0.0) {
            "Harga dasar minuman harus berupa angka valid dan tidak negatif."
        }
    }

    override fun getDetail(): String {
        val suhuPenyajian = if (isDingin) "Dingin" else "Panas"
        return "${ukuran.label} | $suhuPenyajian"
    }
}
