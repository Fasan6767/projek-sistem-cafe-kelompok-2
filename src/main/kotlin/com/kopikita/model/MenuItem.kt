// Penanggung jawab: Syaroful Haidar Sudaryono | Domain Developer
package com.kopikita.model

/**
 * Kelas abstrak yang menjadi dasar seluruh produk di katalog KopiKita.
 * Data umum menu disimpan di sini, sedangkan detail kategori disediakan
 * oleh masing-masing subclass.
 */
abstract class MenuItem(
    val id: String,
    val nama: String,
    val harga: Double,
    stokAwal: Int
) {
    /** Alias kompatibilitas: service dan UI menggunakan istilah kode. */
    val kode: String
        get() = id

    /** Stok dapat dibaca dari luar, tetapi hanya dapat diubah melalui method kelas. */
    var stok: Int = stokAwal
        protected set

    /** Nama kategori menu untuk kebutuhan katalog dan filter. */
    abstract val kategori: String

    init {
        require(id.isNotBlank()) { "ID menu tidak boleh kosong." }
        require(nama.isNotBlank()) { "Nama menu tidak boleh kosong." }
        require(harga.isFinite() && harga >= 0.0) {
            "Harga menu harus berupa angka valid dan tidak boleh negatif."
        }
        require(stokAwal >= 0) { "Stok awal tidak boleh negatif." }
    }

    /** Mengembalikan informasi khusus dari jenis menu. */
    abstract fun getDetail(): String

    /** Alias kompatibilitas untuk service dan UI yang memakai nama method lama. */
    fun infoKategori(): String = kategori

    /** Alias kompatibilitas untuk UI yang menampilkan detail varian. */
    fun deskripsiDetail(): String = getDetail()

    /** Mengurangi stok setelah transaksi berhasil. */
    open fun kurangiStok(jumlah: Int) {
        require(jumlah > 0) { "Jumlah pengurangan stok harus lebih dari 0." }
        check(jumlah <= stok) {
            "Stok $nama tidak mencukupi. Tersedia: $stok, diminta: $jumlah."
        }
        stok -= jumlah
    }

    /** Menambah stok ketika ada restock. */
    open fun tambahStok(jumlah: Int) {
        require(jumlah > 0) { "Jumlah penambahan stok harus lebih dari 0." }
        check(stok <= Int.MAX_VALUE - jumlah) {
            "Penambahan stok $nama melebihi batas maksimum."
        }
        stok += jumlah
    }

    /** Memeriksa ketersediaan tanpa mengubah stok. */
    fun stokMencukupi(jumlah: Int): Boolean {
        require(jumlah > 0) { "Jumlah yang diperiksa harus lebih dari 0." }
        return stok >= jumlah
    }

    override fun toString(): String =
        "[$kode] $nama - Rp ${"%,.0f".format(harga)} (Stok: $stok) | ${getDetail()}"
}
