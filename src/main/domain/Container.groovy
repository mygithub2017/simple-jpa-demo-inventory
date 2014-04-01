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



package domain

import domain.event.InventoryEventConsumer
import domain.inventory.GudangRepository
import domain.inventory.ProdukRepository
import domain.pembelian.FakturBeliRepository
import domain.pembelian.PenerimaanBarangRepository
import domain.pembelian.ReceivedNotInvoicedService
import domain.pembelian.SupplierRepository
import domain.pengaturan.PengaturanRepository
import domain.util.PasswordService
import griffon.util.*

class Container {

    public static Container app = new Container()

    public static final String SEMUA = "Semua"

    private GudangRepository gudangRepository
    private ProdukRepository produkRepository
    private SupplierRepository supplierRepository
    private PenerimaanBarangRepository penerimaanBarangRepository
    private FakturBeliRepository fakturBeliRepository
    private PengaturanRepository pengaturanRepository

    private InventoryEventConsumer inventoryEventConsumer

    private ReceivedNotInvoicedService receivedNotInvoicedService
    private PasswordService passwordService

    private Container() {}

    public void setup() {
        // Create repositories
        gudangRepository = new GudangRepository()
        produkRepository = new ProdukRepository()
        supplierRepository = new SupplierRepository()
        penerimaanBarangRepository = new PenerimaanBarangRepository()
        fakturBeliRepository = new FakturBeliRepository()
        pengaturanRepository = new PengaturanRepository()

        // Create services
        receivedNotInvoicedService = new ReceivedNotInvoicedService()
        passwordService = new PasswordService()

        // Create event consumers
        inventoryEventConsumer = new InventoryEventConsumer()
        ApplicationHolder.application.addApplicationEventListener(inventoryEventConsumer)
    }

    public GudangRepository getGudangRepository() {
        gudangRepository
    }

    public ProdukRepository getProdukRepository() {
        produkRepository
    }

    public SupplierRepository getSupplierRepository() {
        supplierRepository
    }

    public PenerimaanBarangRepository getPenerimaanBarangRepository() {
        penerimaanBarangRepository
    }

    public FakturBeliRepository getFakturBeliRepository() {
        fakturBeliRepository
    }

    public ReceivedNotInvoicedService getReceivedNotInvoicedService() {
        receivedNotInvoicedService
    }

    public PasswordService getPasswordService() {
        passwordService
    }

    public PengaturanRepository getPengaturanRepository() {
        pengaturanRepository
    }

    public List searchEnum(Class enumeration) {
        List result = [SEMUA]
        result.addAll(EnumSet.allOf(enumeration))
        result
    }
}