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
import domain.pembelian.FakturBeli
import domain.pembelian.FakturBeliRepository
import org.joda.time.LocalDate

import javax.swing.event.ListSelectionEvent

class HutangController {

    HutangModel model
    def view

    FakturBeliRepository fakturBeliRepository

    void mvcGroupInit(Map args) {
        fakturBeliRepository = Container.app.fakturBeliRepository
        init()
        search()
    }

    def init = {
        execInsideUISync {
            model.tanggalMulaiSearch = LocalDate.now().minusMonths(1)
            model.tanggalSelesaiSearch = LocalDate.now()
        }
    }

    def search = {
        List result = fakturBeliRepository.cariHutang(model.tanggalMulaiSearch, model.tanggalSelesaiSearch, model.nomorSearch, model.supplierSearch)
        execInsideUISync {
            model.fakturBeliList.clear()
            model.fakturBeliList.addAll(result)
        }
    }

    def clear = {
        execInsideUISync {
            model.id = null
            model.listPembayaranHutang.clear()

            model.errors.clear()
            view.table.selectionModel.clearSelection()
        }
    }

    def tableSelectionChanged = { ListSelectionEvent event ->
        execInsideUISync {
            if (view.table.selectionModel.isSelectionEmpty()) {
                clear()
            } else {
                FakturBeli selected = view.table.selectionModel.selected[0]
                model.errors.clear()
                model.id = selected.id
                model.listPembayaranHutang.clear()
                model.listPembayaranHutang.addAll(selected.hutang?.listPembayaran)
            }
        }
    }

}
