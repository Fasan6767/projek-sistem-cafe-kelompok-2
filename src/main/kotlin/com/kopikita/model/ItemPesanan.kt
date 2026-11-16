package com.kopikita.model

/**
 * Data class yang merepresentasikan satu baris item dalam pesanan.
 */
data class ItemPesanan(
    val menuItem: MenuItem,
    val kuantitas: Int
) {
    init {
        require(kuantitas > 0) { "Kuantitas pesanan minimal harus 1." }
    }

    /**
     * Subtotal biaya untuk baris item ini
     */
    val subtotal: Double
        get() = menuItem.harga * kuantitas
}
