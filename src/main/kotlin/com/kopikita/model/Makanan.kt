// Penanggung jawab: Syaroful Haidar Sudaryono | Domain Developer
package com.kopikita.model

/** Model produk makanan pada katalog KopiKita. */
class Makanan(
    id: String,
    nama: String,
    harga: Double,
    stokAwal: Int,
    tingkatPedas: Int = 0,
    val isVegetarian: Boolean = false,
    levelPedas: Int? = null,
    val isHangat: Boolean = true
) : MenuItem(id, nama, harga, stokAwal) {

    override val kategori: String = "Makanan"

    /** Kedua nama properti disediakan agar kompatibel dengan model dan service tim. */
    val tingkatPedas: Int = levelPedas ?: tingkatPedas
    val levelPedas: Int
        get() = tingkatPedas

    init {
        require(this.tingkatPedas in 0..5) {
            "Tingkat pedas harus berada pada rentang 0 sampai 5."
        }
    }

    override fun getDetail(): String {
        val statusPedas = if (tingkatPedas == 0) "Tidak pedas" else "Level pedas $tingkatPedas"
        val suhu = if (isHangat) "Disajikan hangat" else "Tidak disajikan hangat"
        val vegetarian = if (isVegetarian) "Vegetarian" else "Non-vegetarian"
        return "$suhu | $vegetarian | $statusPedas"
    }
}
