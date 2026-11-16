package com.kopikita.model

/**
 * Pilihan ukuran minuman kafe
 */
enum class UkuranMinuman(val label: String, val tambahanHarga: Double) {
    REGULER("Reguler", 0.0),
    LARGE("Large", 4000.0)
}

/**
 * Subclass Minuman yang mewarisi MenuItem.
 * Mendukung kustomisasi ukuran dan opsi dingin/panas.
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

    override fun getDetail(): String {
        val suhu = if (isDingin) "🧊 Dingin (Iced)" else "☕ Panas (Hot)"
        return "${ukuran.label} | $suhu"
    }
}
