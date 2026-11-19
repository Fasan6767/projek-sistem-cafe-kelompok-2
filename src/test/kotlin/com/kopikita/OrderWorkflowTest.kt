package com.kopikita

import com.kopikita.payment.PembayaranTunai
import com.kopikita.service.KafeService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Suite Pengujian Alur Transaksi & Validasi Pembayaran Tunai
 * Dibuat oleh: Nasywan Aziz (QA Engineer - 225443048)
 */
class OrderWorkflowTest {
    private lateinit var service: KafeService
    @BeforeEach
    fun setup() {
        service = KafeService()
    }
    @Test
    fun testAlurTransaksiLengkapDariKeranjangHinggaSelesai() {
        // 1. Kasir buat pesanan baru (2 Kopi Susu D01 + 1 Croissant S01)
        val pesanan = service.buatPesanan("Pelanggan VIP", mapOf("D01" to 2, "S01" to 1))
        assertNotNull(pesanan)
        assertEquals(2, pesanan.daftarItem.size)
        // 2. Cek kalkulasi tagihan: (2 x 18.000) + (1 x 22.000) = 58.000
        val totalExpected = 58000.0
        assertEquals(totalExpected, pesanan.hitungTotal(), 0.001)
        // 3. Proses bayar pakai tunai Rp 100.000
        val tunai = PembayaranTunai(uangDibayar = 100000.0)
        val hasil = service.prosesTransaksi(pesanan, tunai)
        // 4. Validasi transaksi sukses dan kembalian pas (100.000 - 58.000 = 42.000)
        assertTrue(hasil, "Transaksi harus sukses")
        assertTrue(pesanan.statusLunas, "Status pesanan harus lunas")
        assertEquals(42000.0, tunai.kembalian, 0.001)
    }
    @Test
    fun testPembayaranDenganUangPasSuksesKembalianNol() {
        val pesanan = service.buatPesanan("Pelanggan Uang Pas", mapOf("D02" to 1))
        val total = pesanan.hitungTotal() // 24.000 (harga dasar 20.000 + biaya ukuran Large 4.000)
        val tunai = PembayaranTunai(uangDibayar = total)
        val hasil = service.prosesTransaksi(pesanan, tunai)
        assertTrue(hasil)
        assertEquals(0.0, tunai.kembalian, 0.001)
    }
}
