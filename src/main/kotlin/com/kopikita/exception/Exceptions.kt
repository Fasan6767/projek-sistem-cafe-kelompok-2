package com.kopikita.exception

/**
 * Kumpulan Custom Exception untuk domain dan logika bisnis KopiKita POS.
 */
open class KopiKitaException(pesan: String) : RuntimeException(pesan)

class MenuTidakDitemukanException(pesan: String) : IllegalArgumentException(pesan)

class StokTidakCukupException(pesan: String) : IllegalStateException(pesan)

class PembayaranGagalException(pesan: String) : IllegalStateException(pesan)
