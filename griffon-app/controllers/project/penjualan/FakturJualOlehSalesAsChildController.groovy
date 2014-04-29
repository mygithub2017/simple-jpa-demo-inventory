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
package project.penjualan

import domain.Container
import domain.penjualan.FakturJualRepository

import java.text.NumberFormat

class FakturJualOlehSalesAsChildController {

    FakturJualOlehSalesAsChildModel model
    def view

    FakturJualRepository fakturJualRepository

    void mvcGroupInit(Map args) {
        fakturJualRepository = Container.app.fakturJualRepository
        model.konsumen = fakturJualRepository.findKonsumenByIdFetchFakturBelumLunas(args.'konsumen'.id)
        init()
    }

    def init = {
        execInsideUISync {
            model.fakturJualOlehSalesList.clear()
            model.fakturJualOlehSalesList.addAll(model.konsumen.listFakturBelumLunas)
            model.informasi = "<html>Konsumen: <strong>${model.konsumen.nama}</strong>  Jumlah Piutang: <strong>${NumberFormat.currencyInstance.format(model.konsumen.jumlahPiutang())}</strong></html>"
        }
    }

}