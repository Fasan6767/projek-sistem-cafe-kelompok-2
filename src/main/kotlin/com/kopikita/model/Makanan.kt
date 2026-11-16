// Penanggung jawab: Haidar | Domain Developer
package com.kopikita.model

/**
 * Model menu makanan.
 * Selain atribut umum dari MenuItem, makanan memiliki tingkat pedas
 * dan penanda apakah menu tersebut cocok untuk vegetarian.
 *
 * Penanggung jawab bagian Domain Model: Haidar
 */
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
            "Tingkat pedas makanan harus berada pada rentang 0 sampai 5."
        }
    }

    override fun getDetail(): String {
        val informasiVegetarian = if (isVegetarian) "Vegetarian" else "Non-Vegetarian"
        val informasiPedas = if (tingkatPedas == 0) "Tidak pedas" else "Level pedas $tingkatPedas"
        return "$informasiVegetarian | $informasiPedas"
    }
}
