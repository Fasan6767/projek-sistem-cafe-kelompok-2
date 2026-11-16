package com.kopikita

import com.kopikita.exception.MenuTidakDitemukanException
import com.kopikita.exception.PembayaranGagalException
import com.kopikita.exception.StokTidakCukupException
import com.kopikita.model.*
import com.kopikita.service.*
import java.util.Scanner

fun main() {
    val scanner = Scanner(System.`in`)
    val kafeService = KafeService()

    println("=================================================================")
    println("           ☕ SELAMAT DATANG DI KOPIKITA POS v1.0.0             ")
    println("         Sistem Kasir & Manajemen Kafe Berbasis OOP Kotlin       ")
    println("=================================================================")

    var running = true
    while (running) {
        println()
        println("╔═══════════════════════════════════════════════════════════════╗")
        println("║                         MENU UTAMA                            ║")
        println("╠═══════════════════════════════════════════════════════════════╣")
        println("║  [1] 📋 Lihat Katalog Menu Lengkap                           ║")
        println("║  [2] 🛒 Buat Transaksi Pesanan Baru                           ║")
        println("║  [3] 📜 Lihat Riwayat Seluruh Transaksi                       ║")
        println("║  [4] 📊 Laporan Pendapatan & Omset Kafe                      ║")
        println("║  [5] 📦 Tambah Stok Menu (Restock)                            ║")
        println("║  [0] 🚪 Keluar dari Aplikasi                                  ║")
        println("╚═══════════════════════════════════════════════════════════════╝")
        print("👉 Masukkan pilihan menu [0-5]: ")

        when (scanner.nextLine().trim()) {
            "1" -> tampilkanKatalogMenu(kafeService, scanner)
            "2" -> buatTransaksiBaru(kafeService, scanner)
            "3" -> tampilkanRiwayatPesanan(kafeService)
            "4" -> tampilkanLaporanPendapatan(kafeService)
            "5" -> restockMenu(kafeService, scanner)
            "0" -> {
                println("\nTerima kasih telah menggunakan KopiKita POS. Sampai jumpa!")
                running = false
            }
            else -> println("⚠️ Pilihan tidak valid! Silakan masukkan angka 0 sampai 5.")
        }
    }
}

private fun tampilkanKatalogMenu(service: KafeService, scanner: Scanner) {
    println("\n--- PILIH TAMPILAN KATALOG ---")
    println("1. Semua Menu")
    println("2. Khusus Makanan")
    println("3. Khusus Minuman")
    println("4. Khusus Camilan")
    print("Pilihan [1-4]: ")
    val pilihan = scanner.nextLine().trim()

    val list = when (pilihan) {
        "2" -> service.getMenuByKategori("Makanan")
        "3" -> service.getMenuByKategori("Minuman")
        "4" -> service.getMenuByKategori("Camilan")
        else -> service.getSemuaMenu()
    }

    println("\n---------------------------------------------------------------------------------------------")
    println("%-6s | %-28s | %-12s | %-8s | %-25s".format("ID", "NAMA MENU", "HARGA", "STOK", "DETAIL SPESIFIK"))
    println("---------------------------------------------------------------------------------------------")
    for (item in list) {
        println(
            "%-6s | %-28s | Rp %,10.0f | %-8d | %-25s".format(
                item.id,
                item.nama,
                item.harga,
                item.stok,
                item.getDetail()
            )
        )
    }
    println("---------------------------------------------------------------------------------------------")
}

private fun buatTransaksiBaru(service: KafeService, scanner: Scanner) {
    println("\n--- TRANSAKSI PESANAN BARU ---")
    print("Nama Pelanggan: ")
    var namaPelanggan = scanner.nextLine().trim()
    while (namaPelanggan.isEmpty()) {
        print("Nama tidak boleh kosong! Masukkan nama pelanggan: ")
        namaPelanggan = scanner.nextLine().trim()
    }

    val pesanan = service.buatPesanan(namaPelanggan)
    println("✅ Pesanan dibuat dengan ID: ${pesanan.idPesanan}")

    var addingItem = true
    while (addingItem) {
        print("\nMasukkan ID Menu yang ingin dipesan (atau ketik 'selesai'): ")
        val inputMenuId = scanner.nextLine().trim()

        if (inputMenuId.equals("selesai", ignoreCase = true)) {
            if (pesanan.items.isEmpty()) {
                println("⚠️ Belum ada item yang ditambahkan. Ketik 'batal' jika ingin membatalkan transaksi.")
                print("Lanjut memesan? (y/batal): ")
                if (scanner.nextLine().trim().equals("batal", ignoreCase = true)) {
                    pesanan.batalkan()
                    println("❌ Transaksi dibatalkan.")
                    return
                }
                continue
            }
            addingItem = false
            break
        }

        print("Jumlah/Kuantitas: ")
        val qtyInput = scanner.nextLine().trim()
        val qty = qtyInput.toIntOrNull()
        if (qty == null || qty <= 0) {
            println("⚠️ Kuantitas tidak valid! Harus berupa angka positif.")
            continue
        }

        try {
            service.tambahItemKePesanan(pesanan, inputMenuId.uppercase(), qty)
            val menu = service.getMenuById(inputMenuId.uppercase())
            println("✨ Ditambahkan: ${menu.nama} x $qty (Subtotal: Rp %,.0f)".format(menu.harga * qty))
        } catch (e: MenuTidakDitemukanException) {
            println("❌ Error: ${e.message}")
        } catch (e: StokTidakCukupException) {
            println("❌ Error: ${e.message}")
        } catch (e: Exception) {
            println("❌ Terjadi kesalahan: ${e.message}")
        }
    }

    // Tampilkan Rincian Tagihan
    println("\n==============================================")
    println("           RINGKASAN PEMESANAN                ")
    println("==============================================")
    println("ID Pesanan   : ${pesanan.idPesanan}")
    println("Pelanggan    : ${pesanan.namaPelanggan}")
    println("Waktu        : ${pesanan.getFormattedWaktu()}")
    println("----------------------------------------------")
    for (item in pesanan.items) {
        println("%-22s x%-3d Rp %,10.0f".format(item.menuItem.nama, item.kuantitas, item.subtotal))
    }
    println("----------------------------------------------")
    println("Subtotal     : Rp %,12.0f".format(pesanan.hitungTotal()))
    println("Pajak (10%)  : Rp %,12.0f".format(pesanan.hitungPajak()))
    println("GRAND TOTAL  : Rp %,12.0f".format(pesanan.hitungGrandTotal()))
    println("==============================================")

    // Proses Pembayaran
    println("\nPILIH METODE PEMBAYARAN:")
    println("1. Tunai (Cash)")
    println("2. QRIS Digital")
    print("Metode [1/2]: ")
    val metodePilihan = scanner.nextLine().trim()

    val metode: MetodePembayaran = when (metodePilihan) {
        "2" -> PembayaranQris()
        else -> PembayaranTunai()
    }

    var paymentSuccess = false
    while (!paymentSuccess) {
        val grandTotal = pesanan.hitungGrandTotal()
        if (metode is PembayaranQris) {
            println("📲 Silakan scan QRIS Kafe untuk nominal Rp %,.0f".format(grandTotal))
            print("Ketik 'konfirmasi' setelah pelanggan selesai scan: ")
            scanner.nextLine()
            try {
                val hasil = service.bayarPesanan(pesanan, metode, grandTotal)
                cetakStruk(pesanan, hasil)
                paymentSuccess = true
            } catch (e: Exception) {
                println("❌ Pembayaran QRIS Gagal: ${e.message}")
                break
            }
        } else {
            print("Masukkan nominal uang tunai yang diterima: Rp ")
            val bayarInput = scanner.nextLine().trim().toDoubleOrNull()
            if (bayarInput == null || bayarInput <= 0) {
                println("⚠️ Masukkan nominal uang yang valid!")
                continue
            }
            try {
                val hasil = service.bayarPesanan(pesanan, metode, bayarInput)
                cetakStruk(pesanan, hasil)
                paymentSuccess = true
            } catch (e: PembayaranGagalException) {
                println("❌ ${e.message}")
                print("Apakah ingin mencoba memasukkan uang lagi? (y/n): ")
                if (!scanner.nextLine().trim().equals("y", ignoreCase = true)) {
                    pesanan.batalkan()
                    println("❌ Transaksi dibatalkan karena pembayaran tidak selesai.")
                    break
                }
            }
        }
    }
}

private fun cetakStruk(pesanan: Pesanan, hasil: HasilPembayaran) {
    println("\n==============================================")
    println("               STRUK PEMBAYARAN               ")
    println("                 KOPIKITA KAFE                ")
    println("==============================================")
    println("No. Struk    : ${pesanan.idPesanan}")
    println("Waktu        : ${pesanan.getFormattedWaktu()}")
    println("Kasir / Meja : Pelanggan [${pesanan.namaPelanggan}]")
    println("----------------------------------------------")
    for (item in pesanan.items) {
        println("%-22s x%-3d Rp %,10.0f".format(item.menuItem.nama, item.kuantitas, item.subtotal))
    }
    println("----------------------------------------------")
    println("Subtotal     : Rp %,12.0f".format(pesanan.hitungTotal()))
    println("Pajak Resto  : Rp %,12.0f".format(pesanan.hitungPajak()))
    println("Grand Total  : Rp %,12.0f".format(hasil.totalTagihan))
    println("Metode Bayar : ${hasil.metode}")
    println("Dibayar      : Rp %,12.0f".format(hasil.jumlahDibayar))
    println("Kembalian    : Rp %,12.0f".format(hasil.kembalian))
    println("----------------------------------------------")
    println("Status       : LUNAS ✅")
    println("Catatan      : ${hasil.catatan}")
    println("==============================================")
    println("    Terima kasih atas kunjungan Anda!         ")
    println("==============================================")
}

private fun tampilkanRiwayatPesanan(service: KafeService) {
    val list = service.getSemuaPesanan()
    println("\n==========================================================================")
    println("                         RIWAYAT PESANAN KAFE                             ")
    println("==========================================================================")
    if (list.isEmpty()) {
        println("Belum ada pesanan yang dibuat.")
        return
    }

    println("%-9s | %-15s | %-20s | %-10s | %-12s".format("ID", "PELANGGAN", "WAKTU", "STATUS", "GRAND TOTAL"))
    println("--------------------------------------------------------------------------")
    for (p in list) {
        println(
            "%-9s | %-15s | %-20s | %-10s | Rp %,10.0f".format(
                p.idPesanan,
                p.namaPelanggan,
                p.getFormattedWaktu(),
                p.status,
                p.hitungGrandTotal()
            )
        )
    }
    println("==========================================================================")
}

private fun tampilkanLaporanPendapatan(service: KafeService) {
    val totalOmset = service.hitungTotalPendapatan()
    val pesananLunas = service.getSemuaPesanan().count { it.status == StatusPesanan.LUNAS }

    println("\n==============================================")
    println("           LAPORAN PENDAPATAN KAFE            ")
    println("==============================================")
    println("Jumlah Transaksi Lunas : $pesananLunas pesanan")
    println("Total Omset Bersih     : Rp %,.0f".format(totalOmset))
    println("==============================================")
}

private fun restockMenu(service: KafeService, scanner: Scanner) {
    println("\n--- RESTOCK MENU GUDANG ---")
    print("Masukkan ID Menu yang ingin ditambah stoknya: ")
    val id = scanner.nextLine().trim().uppercase()

    try {
        val menu = service.getMenuById(id)
        println("Menu ditemukan: ${menu.nama} (Stok saat ini: ${menu.stok})")
        print("Jumlah stok tambahan: ")
        val jumlahInput = scanner.nextLine().trim().toIntOrNull()
        if (jumlahInput == null || jumlahInput <= 0) {
            println("⚠️ Jumlah tambahan harus berupa angka positif.")
            return
        }
        menu.tambahStok(jumlahInput)
        println("✅ Berhasil menambahkan $jumlahInput stok ke ${menu.nama}. Stok sekarang: ${menu.stok}")
    } catch (e: MenuTidakDitemukanException) {
        println("❌ Error: ${e.message}")
    }
}
