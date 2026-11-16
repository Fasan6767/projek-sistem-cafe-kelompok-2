// Penanggung jawab: Haidar | Domain Developer
package com.kopikita.model

/**
 * Model dasar untuk seluruh menu yang dijual di KopiKita.
 *
 * Kelas ini dibuat abstrak karena setiap jenis menu memiliki detail
 * yang berbeda. Data umum seperti identitas, harga, dan stok disimpan
 * di sini agar tidak perlu ditulis ulang pada setiap subclass.
 *
 * Penanggung jawab bagian Domain Model: Haidar
 */
abstract class MenuItem(
    val id: String,
    val nama: String,
    val harga: Double,
    stokAwal: Int
) {

    /** Stok hanya dapat diubah melalui operasi stok yang disediakan kelas ini. */
    var stok: Int = stokAwal
        protected set

    /** Nama kategori yang digunakan ketika menu ditampilkan atau difilter. */
    abstract val kategori: String

    init {
        require(id.isNotBlank()) { "ID menu tidak boleh kosong." }
        require(nama.isNotBlank()) { "Nama menu tidak boleh kosong." }
        require(harga.isFinite() && harga >= 0.0) {
            "Harga menu harus berupa angka valid dan tidak boleh negatif."
        }
        require(stokAwal >= 0) { "Stok awal tidak boleh negatif." }
    }

    /** Menghasilkan keterangan tambahan sesuai jenis menu. */
    abstract fun getDetail(): String

    /**
     * Mengurangi stok ketika item dimasukkan ke pesanan.
     * Operasi ditolak jika jumlah tidak valid atau stok tidak mencukupi.
     */
    open fun kurangiStok(jumlah: Int) {
        require(jumlah > 0) { "Jumlah pengurangan stok harus lebih dari 0." }
        check(jumlah <= stok) {
            "Stok $nama tidak mencukupi. Tersedia: $stok, diminta: $jumlah."
        }
        stok -= jumlah
    }

    /** Menambahkan stok ketika ada penambahan persediaan atau pembatalan pesanan. */
    open fun tambahStok(jumlah: Int) {
        require(jumlah > 0) { "Jumlah penambahan stok harus lebih dari 0." }
        check(stok <= Int.MAX_VALUE - jumlah) { "Jumlah stok melebihi batas maksimum." }
        stok += jumlah
    }

    /** Memeriksa ketersediaan stok tanpa mengubah nilainya. */
    fun stokMencukupi(jumlah: Int): Boolean {
        require(jumlah > 0) { "Jumlah yang diperiksa harus lebih dari 0." }
        return stok >= jumlah
    }

    override fun toString(): String {
        return "[$id] $nama - Rp ${"%,.0f".format(harga)} (Stok: $stok) | ${getDetail()}"
    }
}
