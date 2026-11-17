
// Penanggung jawab: Syaroful Haidar Sudaryono | Domain Developer
package com.kopikita.model

/** Model produk camilan, pastry, dan makanan ringan. */
class Camilan(
    id: String,
    nama: String,
    harga: Double,
    stokAwal: Int,
    val porsi: String = "1 Porsi",
    val isManis: Boolean = true
) : MenuItem(id, nama, harga, stokAwal) {

    override val kategori: String = "Camilan"

    init {
        require(porsi.isNotBlank()) {
            "Informasi porsi camilan tidak boleh kosong."
        }
    }

    override fun getDetail(): String {
        val rasa = if (isManis) "Manis" else "Gurih"
        return "$rasa | ${porsi.trim()}"
    }
}
