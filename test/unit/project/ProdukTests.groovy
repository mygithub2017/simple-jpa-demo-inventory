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

import domain.inventory.Produk
import domain.pengaturan.KeyPengaturan
import griffon.test.GriffonUnitTestCase
import project.pengaturan.PengaturanRepository
import simplejpa.SimpleJpaUtil

class ProdukTests extends GriffonUnitTestCase {

    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    public void testTersediaUntuk() {
        Produk p = new Produk(nama: 'Produk A', hargaDalamKota: 10000, jumlah: 100, jumlahAkanDikirim: 30)
        assertTrue(p.tersediaUntuk(50))
        assertTrue(p.tersediaUntuk(65))
        assertTrue(p.tersediaUntuk(70))
        assertFalse(p.tersediaUntuk(80))
        assertFalse(p.tersediaUntuk(90))
        assertFalse(p.tersediaUntuk(110))
    }

    public void testLevelMinimum() {
        PengaturanRepository pengaturanRepository = new PengaturanRepository()
        pengaturanRepository.cache[KeyPengaturan.LEVEL_MINIMUM_STOK] = 5
        SimpleJpaUtil.instance.repositoryManager = new StubRepositoryManager()
        SimpleJpaUtil.instance.repositoryManager.instances['PengaturanRepository'] = pengaturanRepository

        Produk p1 = new Produk(nama: 'Produk A', levelMinimum: 10)
        assertEquals(10, p1.levelMinimum)

        Produk p2 = new Produk(nama: 'Produk B')
        assertEquals(5, p2.levelMinimum)
    }

}
