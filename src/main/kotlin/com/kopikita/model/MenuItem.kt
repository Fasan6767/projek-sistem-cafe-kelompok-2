package com.kopikita.model

/**
 * Kelas Abstrak dasar untuk semua item menu di Kafe.
 * Menerapkan prinsip Abstraction dan Encapsulation:
 * - Properti stok dilindungi dengan setter terkontrol.
 * - getDetail() merupakan metode abstrak yang wajib di-override oleh setiap subclass.
 */
abstract class MenuItem(
    val id: String,
    val nama: String,
    val harga: Double,
    stokAwal: Int
) {
    /**
     * Stok item menu dengan private set agar perubahan hanya dapat
     * dilakukan melalui metode khusus yang tervalidasi.
     */
    var stok: Int = stokAwal
        protected set

    /**
     * Kategori menu (misal: "Makanan", "Minuman", "Camilan")
     */
    abstract val kategori: String

    /**
     * Menampilkan detail spesifik dari item menu.
     */
    abstract fun getDetail(): String

    /**
     * Mengurangi stok saat pesanan dibuat.
     * @param jumlah Kuantitas yang ingin dikurangi.
     * @throws IllegalArgumentException jika jumlah tidak valid atau stok tidak mencukupi.
     */
    open fun kurangiStok(jumlah: Int) {
        require(jumlah > 0) { "Jumlah pengurangan stok harus lebih dari 0." }
        require(stok >= jumlah) { "Stok $nama tidak mencukupi! Tersedia: $stok, diminta: $jumlah." }
        stok -= jumlah
    }

    /**
     * Menambah stok (misal: restock gudang).
     */
    open fun tambahStok(jumlah: Int) {
        require(jumlah > 0) { "Jumlah penambahan stok harus lebih dari 0." }
        stok += jumlah
    }

    override fun toString(): String {
        return "[$id] $nama - Rp %,.0f (Stok: $stok) | ${getDetail()}".format(harga)
    }
}
