package com.kopikita.model

/**
 * Subclass Camilan yang mewarisi MenuItem.
 * Mewakili snack/pastry/kudapan dengan properti rasa dan porsi sajian.
 */
class Camilan(
    id: String,
    nama: String,
    harga: Double,
    stokAwal: Int,
    val porsi: String = "1 Porsi",
    val isManis: Boolean = true
) : MenuItem(id, nama, harga, stokAwal) {

    override val kategori: String = "Camilan"

    override fun getDetail(): String {
        val rasa = if (isManis) "🧁 Manis" else "🍟 Gurih"
        return "$rasa | $porsi"
    }
}
