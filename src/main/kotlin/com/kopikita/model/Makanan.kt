// Penanggung jawab: Syaroful Haidar Sudaryono | Domain Developer
package com.kopikita.model

/** Model produk makanan pada katalog KopiKita. */
class Makanan(
    id: String,
    nama: String,
    harga: Double,
    stokAwal: Int,
    val tingkatPedas: Int = 0,
    val isVegetarian: Boolean = false
) : MenuItem(id, nama, harga, stokAwal) {

    override val kategori: String = "Makanan"

    init {
        require(tingkatPedas in 0..5) {
            "Tingkat pedas harus berada pada rentang 0 sampai 5."
        }
    }

    override fun getDetail(): String {
        val statusPedas = if (tingkatPedas == 0) "Tidak pedas" else "Level pedas $tingkatPedas"
        val statusVegetarian = if (isVegetarian) "Vegetarian" else "Non-vegetarian"
        return "$statusVegetarian | $statusPedas"
    }
}
