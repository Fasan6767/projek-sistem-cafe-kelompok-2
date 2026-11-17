package com.kopikita

import com.kopikita.model.Camilan
import com.kopikita.model.Makanan
import com.kopikita.model.Minuman
import com.kopikita.payment.PembayaranQRIS
import com.kopikita.payment.PembayaranTunai
import com.kopikita.service.KafeService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Pengujian otomatis JUnit 5 untuk KafeService (US-01 s/d US-05, TC-01 s/d TC-08).
 */
class KafeServiceTest {

    private lateinit var kafeService: KafeService

    @BeforeEach
    fun setUp() {
        kafeService = KafeService()
    }

    @Test
    fun testTampilkanKatalogMenuLengkap() {
        // TC-01 (US-01)
        val katalog = kafeService.daftarMenu
        assertTrue(katalog.isNotEmpty())
        assertTrue(katalog.any { it is Makanan })
        assertTrue(katalog.any { it is Minuman })
        assertTrue(katalog.any { it is Camilan })
    }

    @Test
    fun testBuatPesananMultiItemDanHitungTotal() {
        // TC-02 (US-02)
        // M01 = 28.000 x 2 = 56.000; D01 = 18.000 x 1 = 18.000; Total = 74.000
        val keranjang = mapOf("M01" to 2, "D01" to 1)
        val pesanan = kafeService.buatPesanan("Faishal", keranjang)

        assertEquals("Faishal", pesanan.namaPelanggan)
        assertEquals(2, pesanan.daftarItem.size)
        assertEquals(74000.0, pesanan.hitungTotal(), 0.001)
    }

    @Test
    fun testPembayaranTunaiBerhasilDanHitungKembalian() {
        // TC-03 (US-03)
        val keranjang = mapOf("M01" to 1) // 28.000
        val pesanan = kafeService.buatPesanan("Budi", keranjang)
        val menu = kafeService.cariMenuByKode("M01")!!
        val stokAwal = menu.stok

        val pembayaran = PembayaranTunai(uangDibayar = 50000.0)
        val sukses = kafeService.prosesTransaksi(pesanan, pembayaran)

        assertTrue(sukses)
        assertTrue(pesanan.statusLunas)
        assertEquals(22000.0, pembayaran.kembalian, 0.001)
        assertEquals(stokAwal - 1, menu.stok)
    }

    @Test
    fun testPembayaranTunaiGagalUangKurang() {
        // TC-04 (US-03)
        val keranjang = mapOf("M01" to 1) // 28.000
        val pesanan = kafeService.buatPesanan("Siti", keranjang)
        val menu = kafeService.cariMenuByKode("M01")!!
        val stokAwal = menu.stok

        val pembayaran = PembayaranTunai(uangDibayar = 20000.0)
        val sukses = kafeService.prosesTransaksi(pesanan, pembayaran)

        assertFalse(sukses)
        assertFalse(pesanan.statusLunas)
        assertEquals(stokAwal, menu.stok) // stok tidak berkurang
    }

    @Test
    fun testPembayaranQRISBerhasil() {
        // TC-05 (US-03)
        val keranjang = mapOf("D01" to 2) // 18.000 x 2 = 36.000
        val pesanan = kafeService.buatPesanan("Andi", keranjang)
        val menu = kafeService.cariMenuByKode("D01")!!
        val stokAwal = menu.stok

        val qris = PembayaranQRIS()
        val sukses = kafeService.prosesTransaksi(pesanan, qris)

        assertTrue(sukses)
        assertTrue(pesanan.statusLunas)
        assertEquals(stokAwal - 2, menu.stok)
    }

    @Test
    fun testTolakPesananJikaStokHabis() {
        // TC-06 (US-04)
        val menu = kafeService.cariMenuByKode("S01")!!
        menu.kurangiStok(menu.stok) // habiskan stok

        assertThrows<IllegalStateException> {
            kafeService.buatPesanan("Dewi", mapOf("S01" to 1))
        }
    }

    @Test
    fun testTolakPesananJikaKuantitasMelebihiStok() {
        // TC-07 (US-04)
        val menu = kafeService.cariMenuByKode("S02")!! // stok 25
        assertThrows<IllegalStateException> {
            kafeService.buatPesanan("Rian", mapOf("S02" to 999))
        }
    }

    @Test
    fun testRekapLaporanOmsetHarian() {
        // TC-08 (US-05)
        val p1 = kafeService.buatPesanan("Pelanggan 1", mapOf("M01" to 1)) // 28.000
        val p2 = kafeService.buatPesanan("Pelanggan 2", mapOf("D01" to 1)) // 18.000

        kafeService.prosesTransaksi(p1, PembayaranTunai(50000.0))
        kafeService.prosesTransaksi(p2, PembayaranQRIS())

        assertEquals(46000.0, kafeService.hitungTotalOmsetHarian(), 0.001)
        assertEquals(2, kafeService.riwayatPesanan.size)
    }
}
