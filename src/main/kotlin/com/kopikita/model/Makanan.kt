package com.kopikita.model

/**
 * Subclass Makanan yang mewarisi MenuItem (Inheritance).
 * Memiliki properti spesifik tingkat kepedasan dan status vegetarian.
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

    override fun getDetail(): String {
        val statusVeg = if (isVegetarian) "🌱 Vegetarian" else "🍖 Non-Veg"
        val pedasStr = if (tingkatPedas > 0) "🌶️ Level $tingkatPedas" else "Tidak Pedas"
        return "$statusVeg | $pedasStr"
    }
}
