package com.kopikita.ui

import com.kopikita.model.MenuItem
import com.kopikita.model.Pesanan
import com.kopikita.payment.MetodePembayaran
import com.kopikita.payment.PembayaranQRIS
import com.kopikita.payment.PembayaranTunai
import com.kopikita.service.KafeService
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import java.util.Scanner

class KopiKitaCLI(
    private val kafeService: KafeService = KafeService(),
    private val scanner: Scanner = Scanner(System.`in`)
) {
    private val rupiahFormatter: DecimalFormat = DecimalFormat(
        "Rp #,##0",
        DecimalFormatSymbols(Locale("id", "ID")).apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }
    )

    private fun formatRupiah(nominal: Double): String = rupiahFormatter.format(nominal)

    fun mulai() {
        var berjalan = true
        while (berjalan) {
            tampilkanMenuUtama()
            when (bacaInput("Pilih menu [0-4]: ")) {
                "1" -> tampilkanKatalogMenu()
                "2" -> alurBuatPesananDanTransaksi()
                "3" -> tampilkanLaporanOmsetHarian()
                "4" -> alurTambahStokMenu()
                "0" -> {
                    println("\nMenutup KopiKita POS. Terima kasih telah bertugas hari ini!")
                    berjalan = false
                }
                else -> println("\n[!] Pilihan tidak valid. Masukkan angka 0 sampai 4.")
            }
        }
    }

    private fun tampilkanMenuUtama() {
        println("\n+----------------------------------------------------------------------+")
        println("|                 KOPIKITA POS - SISTEM KASIR KAFE CERDAS              |")
        println("+----------------------------------------------------------------------+")
        println("|  1. Lihat Katalog Menu & Sisa Stok                                   |")
        println("|  2. Buat Pesanan Baru & Pembayaran                                   |")
        println("|  3. Rekap Laporan Omset Harian                                       |")
        println("|  4. Restock / Tambah Stok Menu                                       |")
        println("|  0. Keluar Aplikasi                                                  |")
        println("+----------------------------------------------------------------------+")
    }

    // US-01: Menampilkan Katalog Menu Lengkap beserta Kategori & Status Stok
    fun tampilkanKatalogMenu() {
        val daftarMenu = kafeService.daftarMenu
        if (daftarMenu.isEmpty()) {
            println("\n[!] Katalog menu saat ini kosong.")
            return
        }

        val urutanKategori = listOf("Minuman", "Makanan", "Camilan")
        val menuPerKategori = daftarMenu.groupBy { it.infoKategori() }

        println("\n==========================================================================================")
        println("                                  KATALOG MENU KOPIKITA                                   ")
        println("==========================================================================================")

        for (kategori in urutanKategori) {
            val items = menuPerKategori[kategori].orEmpty()
            if (items.isNotEmpty()) {
                println("\n>>> KATEGORI: ${kategori.uppercase()}")
                cetakGarisTabelMenu()
                println(
                    "| %-6s | %-28s | %-12s | %-10s | %-22s |".format(
                        "KODE", "NAMA MENU", "HARGA", "STOK", "DETAIL VARIAN"
                    )
                )
                cetakGarisTabelMenu()

                for (item in items) {
                    val infoStok = if (item.stok == 0) "[HABIS]" else "${item.stok} porsi"
                    println(
                        "| %-6s | %-28s | %-12s | %-10s | %-22s |".format(
                            item.kode,
                            potongTeks(item.nama, 28),
                            formatRupiah(item.harga),
                            infoStok,
                            potongTeks(item.deskripsiDetail(), 22)
                        )
                    )
                }
                cetakGarisTabelMenu()
            }
        }
    }

    private fun cetakGarisTabelMenu() {
        println("+--------+------------------------------+--------------+------------+------------------------+")
    }

    // US-02, US-03, US-04: Buat Pesanan, Validasi Stok, & Proses Pembayaran
    private fun alurBuatPesananDanTransaksi() {
        tampilkanKatalogMenu()

        val namaPelanggan = bacaInput("\nMasukkan Nama Pelanggan (atau '0' untuk batal): ")
        if (namaPelanggan == "0") return
        if (namaPelanggan.isBlank()) {
            println("[!] Nama pelanggan tidak boleh kosong.")
            return
        }

        val keranjang = linkedMapOf<String, Int>()
        println("\n--- INPUT ITEM PESANAN ---")
        println("Ketik kode menu untuk menambah, 'HAPUS' untuk menghapus item, atau 'LANJUT' ke pembayaran.")

        while (true) {
            val inputKode = bacaInput("\nKode Menu / [HAPUS] / [LANJUT]: ").uppercase()

            when (inputKode) {
                "LANJUT" -> {
                    if (keranjang.isEmpty()) {
                        println("[!] Keranjang masih kosong. Tambahkan minimal 1 menu atau ketik 'BATAL'.")
                        continue
                    }
                    break
                }
                "BATAL" -> {
                    println("[!] Pembuatan pesanan dibatalkan.")
                    return
                }
                "HAPUS" -> {
                    hapusItemDariKeranjang(keranjang)
                    continue
                }
            }

            val menu = kafeService.cariMenuByKode(inputKode)
            if (menu == null) {
                println("[!] Kode menu '$inputKode' tidak ditemukan di katalog.")
                continue
            }

            if (menu.stok == 0) {
                println("[!] Menu '${menu.nama}' (${menu.kode}) berstatus [HABIS] dan tidak dapat dipesan.")
                continue
            }

            val qtyStr = bacaInput("Jumlah beli '${menu.nama}' (Tersedia: ${menu.stok}): ")
            val qty = qtyStr.toIntOrNull()
            if (qty == null || qty <= 0) {
                println("[!] Kuantitas harus berupa angka bulat lebih dari 0.")
                continue
            }

            val qtySebelumnya = keranjang[menu.kode] ?: 0
            val totalQty = qtySebelumnya + qty
            if (totalQty > menu.stok) {
                println(
                    "[!] Stok tidak mencukupi! Sisa stok '${menu.nama}': ${menu.stok}, total di keranjang akan menjadi: $totalQty."
                )
                continue
            }

            keranjang[menu.kode] = totalQty
            println("[+] Berhasil menambahkan ${menu.nama} x$qty (Total di keranjang: $totalQty).")
            tampilkanPreviewKeranjang(keranjang)
        }

        val pesanan: Pesanan = try {
            kafeService.buatPesanan(namaPelanggan, keranjang)
        } catch (e: IllegalStateException) {
            println("\n[!] Pesanan ditolak sistem: ${e.message}")
            return
        } catch (e: IllegalArgumentException) {
            println("\n[!] Data pesanan tidak valid: ${e.message}")
            return
        }

        tampilkanRingkasanTagihan(pesanan)

        val metodePembayaran = prosesInputMetodePembayaran(pesanan.hitungTotal()) ?: run {
            println("\n[!] Transaksi dibatalkan kasir. Stok menu tetap aman dan tidak berkurang.")
            return
        }

        val transaksiSukses = kafeService.prosesTransaksi(pesanan, metodePembayaran)
        if (transaksiSukses) {
            cetakStrukTransaksi(pesanan, metodePembayaran)
        } else {
            println("\n[!] Transaksi gagal diproses. Periksa kembali nominal pembayaran atau ketersediaan stok.")
        }
    }

    private fun hapusItemDariKeranjang(keranjang: MutableMap<String, Int>) {
        if (keranjang.isEmpty()) {
            println("[!] Keranjang masih kosong.")
            return
        }
        val kodeHapus = bacaInput("Masukkan kode menu yang ingin dihapus dari keranjang: ").uppercase()
        if (keranjang.remove(kodeHapus) != null) {
            println("[+] Item '$kodeHapus' dihapus dari keranjang.")
        } else {
            println("[!] Item '$kodeHapus' tidak ada di dalam keranjang.")
        }
    }

    private fun tampilkanPreviewKeranjang(keranjang: Map<String, Int>) {
        println("\n  [Keranjang Sementara]")
        var estimasiTotal = 0.0
        for ((kode, qty) in keranjang) {
            val menu = kafeService.cariMenuByKode(kode) ?: continue
            val subtotal = menu.harga * qty
            estimasiTotal += subtotal
            println("  - %-25s x%-2d = %s".format(menu.nama, qty, formatRupiah(subtotal)))
        }
        println("  Estimasi Subtotal: ${formatRupiah(estimasiTotal)}")
    }

    private fun tampilkanRingkasanTagihan(pesanan: Pesanan) {
        println("\n+----------------------------------------------------------------------+")
        println("|                     RINGKASAN KERANJANG PESANAN                      |")
        println("+----------------------------------------------------------------------+")
        println("| ID Pesanan : %-55s |".format(pesanan.idPesanan))
        println("| Pelanggan  : %-55s |".format(pesanan.namaPelanggan))
        println("+--------------------------------------+-------+-----------------------+")
        println("| NAMA ITEM                            | QTY   | SUBTOTAL              |")
        println("+--------------------------------------+-------+-----------------------+")
        for (item in pesanan.daftarItem) {
            println(
                "| %-36s | %-5d | %-21s |".format(
                    potongTeks(item.menu.nama, 36),
                    item.kuantitas,
                    formatRupiah(item.hitungSubtotal())
                )
            )
        }
        println("+--------------------------------------+-------+-----------------------+")
        println("| TOTAL TAGIHAN                        |       | %-21s |".format(formatRupiah(pesanan.hitungTotal())))
        println("+--------------------------------------+-------+-----------------------+")
    }

    private fun prosesInputMetodePembayaran(totalTagihan: Double): MetodePembayaran? {
        while (true) {
            println("\nPilih Metode Pembayaran:")
            println("  1. Tunai")
            println("  2. QRIS")
            println("  0. Batalkan Pesanan")

            when (bacaInput("Pilihan [0-2]: ")) {
                "1" -> {
                    val nominalStr = bacaInput("Masukkan nominal uang tunai (contoh: 50000): ")
                    val uangBayar = nominalStr.toDoubleOrNull()
                    if (uangBayar == null || uangBayar < totalTagihan) {
                        println(
                            "[!] Nominal uang tidak mencukupi. Total tagihan adalah ${formatRupiah(totalTagihan)}."
                        )
                    } else {
                        return PembayaranTunai(uangBayar)
                    }
                }
                "2" -> {
                    val pembayaranQRIS = PembayaranQRIS()
                    println("\n--- VERIFIKASI PEMBAYARAN QRIS ---")
                    println("Kode Referensi : ${pembayaranQRIS.kodeReferensi}")
                    println("Nominal Pas    : ${formatRupiah(totalTagihan)}")
                    val konfirmasi = bacaInput("Apakah pembayaran QRIS sudah masuk? (Y/N): ")
                    if (konfirmasi.equals("Y", ignoreCase = true)) {
                        return pembayaranQRIS
                    } else {
                        println("[!] Pembayaran QRIS belum dikonfirmasi.")
                    }
                }
                "0" -> return null
                else -> println("[!] Pilihan metode pembayaran tidak valid.")
            }
        }
    }

    private fun cetakStrukTransaksi(pesanan: Pesanan, metode: MetodePembayaran) {
        println("\n======================================================================")
        println("                        STRUK PEMBAYARAN LUNAS                        ")
        println("======================================================================")
        println("ID Pesanan   : ${pesanan.idPesanan}")
        println("Pelanggan    : ${pesanan.namaPelanggan}")
        println("----------------------------------------------------------------------")
        for (item in pesanan.daftarItem) {
            println(
                "%-32s x%-3d  %15s".format(
                    potongTeks(item.menu.nama, 32),
                    item.kuantitas,
                    formatRupiah(item.hitungSubtotal())
                )
            )
        }
        println("----------------------------------------------------------------------")
        println("Total Tagihan: ${formatRupiah(pesanan.hitungTotal())}")
        println(metode.getDetailStruk())
        println("Status       : LUNAS (Stok inventaris telah diperbarui)")
        println("======================================================================")
    }

    // US-05: Rekapitulasi Laporan Omset Harian
    private fun tampilkanLaporanOmsetHarian() {
        val riwayat = kafeService.riwayatPesanan
        val totalOmset = kafeService.hitungTotalOmsetHarian()

        println("\n+----------------------------------------------------------------------+")
        println("|                     LAPORAN OMSET HARIAN KOPIKITA                    |")
        println("+----------------------------------------------------------------------+")
        println("| Total Transaksi Sukses : %-43s |".format("${riwayat.size} transaksi"))
        println("| Total Pendapatan       : %-43s |".format(formatRupiah(totalOmset)))
        println("+------------+----------------------+------------+---------------------+")
        println("| ID PESANAN | NAMA PELANGGAN       | METODE     | TOTAL TRANSAKSI     |")
        println("+------------+----------------------+------------+---------------------+")

        if (riwayat.isEmpty()) {
            println("| %-68s |".format("Belum ada transaksi yang selesai hari ini."))
        } else {
            for (pesanan in riwayat) {
                val metode = pesanan.metodePembayaran?.namaMetode ?: "-"
                println(
                    "| %-10s | %-20s | %-10s | %-19s |".format(
                        pesanan.idPesanan,
                        potongTeks(pesanan.namaPelanggan, 20),
                        metode,
                        formatRupiah(pesanan.hitungTotal())
                    )
                )
            }
        }
        println("+------------+----------------------+------------+---------------------+")
    }

    // Utilitas Tambahan: Restock Menu
    private fun alurTambahStokMenu() {
        tampilkanKatalogMenu()
        val kode = bacaInput("\nMasukkan Kode Menu yang akan ditambah stoknya (atau '0' batal): ")
        if (kode == "0") return

        val menu: MenuItem = kafeService.cariMenuByKode(kode) ?: run {
            println("[!] Kode menu '$kode' tidak ditemukan.")
            return
        }

        val tambahan = bacaInput("Jumlah tambahan stok untuk '${menu.nama}' (Stok saat ini: ${menu.stok}): ").toIntOrNull()
        if (tambahan == null || tambahan <= 0) {
            println("[!] Jumlah tambahan stok harus berupa angka lebih dari 0.")
            return
        }

        menu.tambahStok(tambahan)
        println("[+] Stok '${menu.nama}' berhasil ditambah menjadi ${menu.stok} porsi.")
    }

    private fun bacaInput(prompt: String): String {
        print(prompt)
        return if (scanner.hasNextLine()) scanner.nextLine().trim() else ""
    }

    private fun potongTeks(teks: String, maxPanjang: Int): String {
        return if (teks.length <= maxPanjang) teks else teks.take(maxPanjang - 3) + "..."
    }
}