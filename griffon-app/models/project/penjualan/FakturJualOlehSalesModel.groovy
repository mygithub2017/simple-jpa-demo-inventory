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
package project.penjualan

import domain.faktur.ItemFaktur
import domain.inventory.ItemBarang
import domain.penjualan.*
import ca.odell.glazedlists.*
import org.jdesktop.swingx.combobox.ListComboBoxModel
import org.joda.time.*
import util.SwingHelper

class FakturJualOlehSalesModel {

    FakturJualOlehSalesMode mode
    @Bindable boolean showNilaiUang = true
    @Bindable boolean showFakturJual = true
    @Bindable boolean allowAddFakturJual = true
    @Bindable boolean allowPrint = true

    @Bindable Long id
    @Bindable String nomor
    @Bindable LocalDate tanggal
    @Bindable BigDecimal diskonPotonganPersen
    @Bindable BigDecimal diskonPotonganLangsung
    @Bindable String keterangan
    @Bindable StatusFakturJual status
    @Bindable Konsumen konsumen
    @Bindable Boolean kirimDariGudangUtama
    @Bindable Boolean poinBerlaku = true
    List<ItemFaktur> listItemFaktur = []
    List<ItemBarang> listBonus = []

    @Bindable String nomorSearch
    @Bindable String salesSearch
    @Bindable String konsumenSearch
    @Bindable LocalDate tanggalMulaiSearch
    @Bindable LocalDate tanggalSelesaiSearch
    ListComboBoxModel statusSearch = new ListComboBoxModel(SwingHelper.searchEnum(StatusFakturJual))

    BasicEventList<FakturJualOlehSales> fakturJualOlehSalesList = new BasicEventList<>()

    @Bindable String informasi
    @Bindable String informasiBonus

    @Bindable String created
    @Bindable String modified
    @Bindable String createdBy
    @Bindable String modifiedBy

}