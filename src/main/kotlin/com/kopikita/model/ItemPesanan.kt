// Penanggung jawab: Haidar | Domain Developer
package com.kopikita.model

/**
 * Satu baris menu yang dipesan pelanggan, berisi objek menu dan kuantitasnya.
 * Subtotal dihitung dari harga menu dikalikan jumlah yang dipesan.
 *
 * Penanggung jawab bagian Domain Model: Haidar
 */
data class ItemPesanan(
    val menuItem: MenuItem,
    val kuantitas: Int
) {

    val subtotal: Double = menuItem.harga * kuantitas

    init {
        require(kuantitas > 0) { "Kuantitas pesanan minimal harus 1." }
        require(subtotal.isFinite()) { "Subtotal item berada di luar batas perhitungan." }
    }

    override fun toString(): String {
        return "${menuItem.nama} x $kuantitas = Rp ${"%,.0f".format(subtotal)}"
    }
}
