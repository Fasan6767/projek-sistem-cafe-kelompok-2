package com.kopikita.exception

/**
 * Exception khusus ketika ID menu yang diminta tidak terdaftar di sistem.
 */
class MenuTidakDitemukanException(val menuId: String) : 
    Exception("Menu dengan ID '$menuId' tidak ditemukan dalam katalog kafe.")

/**
 * Exception khusus ketika kuantitas pesanan melebihi stok yang tersedia.
 */
class StokTidakCukupException(val namaMenu: String, val stokTersedia: Int, val kuantitasDiminta: Int) : 
    Exception("Stok tidak mencukupi untuk '$namaMenu'! Tersedia: $stokTersedia, Diminta: $kuantitasDiminta.")

/**
 * Exception khusus ketika proses transaksi pembayaran gagal (uang kurang / QRIS tidak sah).
 */
class PembayaranGagalException(pesan: String) : 
    Exception("Transaksi pembayaran ditolak: $pesan")
