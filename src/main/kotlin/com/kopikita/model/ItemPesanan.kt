// Penanggung jawab: Syaroful Haidar Sudaryono | Domain Developer
package com.kopikita.model

/** Satu baris item di dalam pesanan pelanggan. */
data class ItemPesanan(
    val menuItem: MenuItem,
    val kuantitas: Int
) {
    /** Alias kompatibilitas: service dan UI memakai nama menu. */
    val menu: MenuItem
        get() = menuItem

    /** Subtotal merupakan harga satuan dikalikan kuantitas. */
    val subtotal: Double = menuItem.harga * kuantitas

    init {
        require(kuantitas > 0) { "Kuantitas pesanan minimal harus 1." }
        require(subtotal.isFinite()) { "Subtotal item berada di luar batas perhitungan." }
    }

    fun hitungSubtotal(): Double = subtotal

    override fun toString(): String =
        "${menuItem.nama} x $kuantitas = Rp ${"%,.0f".format(subtotal)}"
}
