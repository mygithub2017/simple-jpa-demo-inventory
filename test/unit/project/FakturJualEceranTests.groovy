/*
 * Copyright 2014 Jocki Hendry.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package project

import domain.Container
import domain.exception.DataTidakBolehDiubah
import domain.faktur.ItemFaktur
import domain.inventory.Gudang
import domain.inventory.ItemBarang
import domain.inventory.Produk
import domain.penjualan.FakturJualEceran
import domain.penjualan.NomorService
import domain.penjualan.StatusFakturJual
import griffon.test.GriffonUnitTestCase
import org.joda.time.LocalDate

class FakturJualEceranTests extends GriffonUnitTestCase {

    private static final Gudang gudangUtama = new Gudang()

    protected void setUp() {
        Container.app.gudangRepository.metaClass.cariGudangUtama = { gudangUtama }
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    public void testAntar() {
        FakturJualEceran f = new FakturJualEceran(tanggal: LocalDate.now())
        Produk produkA = new Produk('Produk A', 10000, 50)
        Produk produkB = new Produk('Produk B',  9000, 50)
        f.tambah(new ItemFaktur(produkA, 20))
        f.tambah(new ItemFaktur(produkB, 15))

        Container.app.nomorService.nomorUrutTerakhir[NomorService.TIPE.PENGELUARAN_BARANG] = 5
        f.antar()

        assertEquals(StatusFakturJual.DIANTAR, f.status)
        assertNotNull(f.pengeluaranBarang)
        assertEquals(String.format('000006-SJ-KB-%s', LocalDate.now().toString('MMyyyy')), f.pengeluaranBarang.nomor)
        assertEquals(LocalDate.now(), f.pengeluaranBarang.tanggal)
        assertEquals(gudangUtama, f.pengeluaranBarang.gudang)
        assertNull(f.pengeluaranBarang.alamatTujuan)
        assertNull(f.pengeluaranBarang.namaSupir)
        assertNull(f.pengeluaranBarang.buktiTerima)
        assertEquals(2, f.pengeluaranBarang.items.size())
        assertTrue(f.pengeluaranBarang.items.contains(new ItemBarang(produkA, 20)))
        assertTrue(f.pengeluaranBarang.items.contains(new ItemBarang(produkB, 15)))
    }

    public void testBayar() {
        FakturJualEceran f = new FakturJualEceran(tanggal: LocalDate.now())
        Produk produkA = new Produk('Produk A', 10000, 50)
        Produk produkB = new Produk('Produk B',  9000, 50)
        f.tambah(new ItemFaktur(produkA, 20))
        f.tambah(new ItemFaktur(produkB, 15))
        shouldFail(DataTidakBolehDiubah) {
            f.bayar()
        }
        f.antar()
        f.bayar()
        assertEquals(StatusFakturJual.LUNAS, f.status)
    }

}
