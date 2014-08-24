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

import domain.inventory.Gudang
import domain.inventory.Produk
import domain.pembelian.PenerimaanBarang
import domain.pembelian.Supplier
import domain.retur.BarangRetur
import domain.retur.ReturBeli
import griffon.core.GriffonApplication
import griffon.core.GriffonService
import griffon.core.ServiceManager
import griffon.test.GriffonUnitTestCase
import griffon.test.mock.MockGriffonApplication
import griffon.util.ApplicationHolder
import org.joda.time.LocalDate
import project.inventory.GudangRepository
import project.user.NomorService
import simplejpa.SimpleJpaUtil

class ReturBeliTests extends GriffonUnitTestCase {

    private Gudang gudangUtama = new Gudang(utama: true)

    protected void setUp() {
        super.setUp()
        super.registerMetaClass(GudangRepository)
        GudangRepository.metaClass.cariGudangUtama = { gudangUtama }
        SimpleJpaUtil.instance.repositoryManager = new StubRepositoryManager()
        SimpleJpaUtil.instance.repositoryManager.instances['GudangRepository'] = new GudangRepository()
        MockGriffonApplication app = new MockGriffonApplication()
        app.serviceManager = new ServiceManager() {

            NomorService nomorService = new NomorService()

            @Override
            Map<String, GriffonService> getServices() {
                [:]
            }

            @Override
            GriffonService findService(String name) {
                if (name == 'Nomor') {
                    return nomorService
                }
                null
            }

            @Override
            GriffonApplication getApp() {
                return null
            }
        }
        ApplicationHolder.application = app
    }

    protected void tearDown() {
        super.tearDown()
    }

    public void testTukarBaru() {
        Produk produk1 = new Produk()
        Produk produk2 = new Produk()
        Produk produk3 = new Produk()
        Supplier supplier = new Supplier()
        ReturBeli returBeli = new ReturBeli(supplier: supplier)
        returBeli.tambah(new BarangRetur(produk: produk1, jumlah: 10, tukar: true))
        returBeli.tambah(new BarangRetur(produk: produk2, jumlah: 20, tukar: true))
        returBeli.tambah(new BarangRetur(produk3, 30))

        PenerimaanBarang penerimaanBarang = returBeli.tukarBaru()
        assertEquals(penerimaanBarang, returBeli.penerimaanBarang)
        assertNotNull(penerimaanBarang.nomor)
        assertEquals(LocalDate.now(), penerimaanBarang.tanggal)
        assertEquals(gudangUtama, penerimaanBarang.gudang)
        assertEquals(2, penerimaanBarang.items.size())
        assertEquals(produk1, penerimaanBarang.items[0].produk)
        assertEquals(10, penerimaanBarang.items[0].jumlah)
        assertEquals(produk2, penerimaanBarang.items[1].produk)
        assertEquals(20, penerimaanBarang.items[1].jumlah)
    }

}
