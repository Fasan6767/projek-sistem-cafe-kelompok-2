package com.kopikita

import com.kopikita.exception.MenuTidakDitemukanException
import com.kopikita.exception.PembayaranGagalException
import com.kopikita.exception.StokTidakCukupException
import com.kopikita.model.StatusPesanan
import com.kopikita.service.KafeService
import com.kopikita.service.PembayaranQris
import com.kopikita.service.PembayaranTunai
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Unit Test Suite untuk KopiKita POS (Awan - QA Engineer)
 * Menguji skenario sukses, skenario kegagalan, validasi batas, dan Custom Exception.
 */
class KafeServiceTest {

    private lateinit var service: KafeService

    @BeforeEach
    fun setUp() {
        service = KafeService()
    }

    @Test
    fun testHitungTotalDanPajakPesanan() {
        val pesanan = service.buatPesanan("Budi")
        // D01 Kopi Susu Gula Aren = Rp 18.000 x 2 = Rp 36.000
        service.tambahItemKePesanan(pesanan, "D01", 2)
        // M01 Nasi Goreng = Rp 28.000 x 1 = Rp 28.000
        service.tambahItemKePesanan(pesanan, "M01", 1)

        val totalExpected = 36000.0 + 28000.0 // 64.000
        val pajakExpected = totalExpected * 0.10 // 6.400
        val grandTotalExpected = totalExpected + pajakExpected // 70.400

        assertEquals(totalExpected, pesanan.hitungTotal(), 0.001)
        assertEquals(pajakExpected, pesanan.hitungPajak(), 0.001)
        assertEquals(grandTotalExpected, pesanan.hitungGrandTotal(), 0.001)
    }

    @Test
    fun testStokBerkurangSaatPesananBerhasil() {
        val menu = service.getMenuById("D01")
        val stokAwal = menu.stok

        val pesanan = service.buatPesanan("Citra")
        service.tambahItemKePesanan(pesanan, "D01", 3)

        assertEquals(stokAwal - 3, menu.stok)
    }

    @Test
    fun testStokTidakCukupMelemparException() {
        val pesanan = service.buatPesanan("Dewi")
        val menu = service.getMenuById("M03") // stok awal 10
        
        assertThrows<StokTidakCukupException> {
            service.tambahItemKePesanan(pesanan, "M03", 999)
        }
    }

    @Test
    fun testMenuTidakDitemukanMelemparException() {
        val pesanan = service.buatPesanan("Eko")

        assertThrows<MenuTidakDitemukanException> {
            service.tambahItemKePesanan(pesanan, "KODE_PALSU_99", 1)
        }
    }

    @Test
    fun testPembayaranTunaiUangKurangMelemparException() {
        val pesanan = service.buatPesanan("Fajar")
        service.tambahItemKePesanan(pesanan, "S03", 1) // 15.000 + pajak 1.500 = 16.500

        val tunai = PembayaranTunai()
        assertThrows<PembayaranGagalException> {
            service.bayarPesanan(pesanan, tunai, 10000.0) // Uang kurang
        }
    }

    @Test
    fun testPembayaranQrisSuksesDanStatusLunas() {
        val pesanan = service.buatPesanan("Gita")
        service.tambahItemKePesanan(pesanan, "S01", 1)

        val qris = PembayaranQris()
        val grandTotal = pesanan.hitungGrandTotal()
        val hasil = service.bayarPesanan(pesanan, qris, grandTotal)

        assertTrue(hasil.sukses)
        assertEquals(StatusPesanan.LUNAS, pesanan.status)
        assertEquals(grandTotal, service.hitungTotalPendapatan(), 0.001)
    }

    @Test
    fun testBatalkanPesananMengembalikanStok() {
        val menu = service.getMenuById("M02")
        val stokAwal = menu.stok

        val pesanan = service.buatPesanan("Hendra")
        service.tambahItemKePesanan(pesanan, "M02", 4)
        assertEquals(stokAwal - 4, menu.stok)

        pesanan.batalkan()
        assertEquals(stokAwal, menu.stok, "Stok harus kembali utuh saat pesanan dibatalkan")
        assertEquals(StatusPesanan.DIBATALKAN, pesanan.status)
    }
}
