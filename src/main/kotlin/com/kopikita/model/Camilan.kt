// Penanggung jawab: Haidar | Domain Developer
package com.kopikita.model

/**
 * Model untuk menu camilan seperti pastry, kudapan, dan makanan ringan.
 * Atribut porsi dan jenis rasa membedakan camilan dari kategori menu lain.
 *
 * Penanggung jawab bagian Domain Model: Haidar
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

    init {
        require(porsi.isNotBlank()) { "Informasi porsi camilan tidak boleh kosong." }
    }

    override fun getDetail(): String {
        val jenisRasa = if (isManis) "Manis" else "Gurih"
        return "$jenisRasa | ${porsi.trim()}"
    }
}
