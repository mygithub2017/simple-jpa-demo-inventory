/*
 * Copyright 2015 Jocki Hendry.
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

package project.inventory

import ca.odell.glazedlists.*
import ca.odell.glazedlists.swing.DefaultEventComboBoxModel
import ca.odell.glazedlists.swing.GlazedListsSwing
import domain.inventory.Produk
import domain.inventory.Satuan
import domain.inventory.StokProduk
import domain.pembelian.Supplier

class ProdukModel {

    @Bindable boolean allowTambahProduk
    @Bindable boolean showReturOnly
    Supplier supplierSearch

    @Bindable Long id
	@Bindable String nama
	@Bindable BigDecimal hargaDalamKota
    @Bindable BigDecimal hargaLuarKota
    @Bindable BigDecimal ongkosKirimBeli
    BasicEventList<Satuan> satuanList = new BasicEventList<>()
    @Bindable Integer poin
    @Bindable Integer levelMinimum
    @Bindable Integer jumlahAkanDikirim
    @Bindable String keterangan
    @Bindable DefaultEventComboBoxModel<Satuan> satuan = GlazedListsSwing.eventComboBoxModelWithThreadProxyList(satuanList)
    BasicEventList<Supplier> supplierList = new BasicEventList<>()
    @Bindable DefaultEventComboBoxModel<Supplier> supplier = GlazedListsSwing.eventComboBoxModelWithThreadProxyList(supplierList)

    @Bindable String namaSearch
    @Bindable String searchMessage
	List<StokProduk> daftarStok = []

    BasicEventList<Produk> produkList = new BasicEventList<>()

    @Bindable boolean popupMode = false

}