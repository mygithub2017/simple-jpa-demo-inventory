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

import domain.labarugi.JenisTransaksiKas
import domain.labarugi.KategoriKas
import domain.labarugi.SaldoKas
import org.joda.time.LocalDate
import project.labarugi.KategoriKasService
import simplejpa.testing.DbUnitTestCase

class KategoriKasServiceTest extends DbUnitTestCase {

    KategoriKasService kategoriKasService

    protected void setUp() {
        super.setUp()
        setUpDatabase("/project/data_laba_rugi.xlsx")
        kategoriKasService = app.serviceManager.findService('KategoriKas')
    }

    void testRefreshSaldoKas() {
        kategoriKasService.withTransaction {
            KategoriKas k = findKategoriKasById(-1l)
            JenisTransaksiKas dalamKota = findJenisTransaksiKasById(-1l)
            k.listSaldoKas << new SaldoKas(1,2014, 10000, dalamKota)
            kategoriKasService.refreshSaldoKas(k)
        }

        KategoriKas k = kategoriKasService.findKategoriKasById(-1l)
        JenisTransaksiKas dalamKota = kategoriKasService.findJenisTransaksiKasById(-1l)
        JenisTransaksiKas luarKota = kategoriKasService.findJenisTransaksiKasById(-2l)
        assertEquals(2, k.listSaldoKas.size())
        assertEquals(10000, k.saldo(1, 2014, dalamKota))
        assertEquals(12000, k.saldo(1, 2014, luarKota))
    }

    void testTotalPendapatanDanPengeluaran() {
        assertEquals(22000, kategoriKasService.totalPendapatan(LocalDate.parse('2014-01-01'),
            LocalDate.parse('2014-01-31')).toInteger())
        assertEquals(0, kategoriKasService.totalPengeluaran(LocalDate.parse('2014-01-01'),
            LocalDate.parse('2014-01-31')).toInteger())
    }

}