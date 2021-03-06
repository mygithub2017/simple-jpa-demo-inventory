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
package project.labarugi

import domain.labarugi.CacheGlobal
import org.joda.time.LocalDate
import javax.swing.SwingUtilities
import java.text.NumberFormat

@SuppressWarnings("GroovyUnusedDeclaration")
class NilaiInventoryController {

    NilaiInventoryModel model
    def view
    LabaRugiService labaRugiService

    void mvcGroupInit(Map args) {
        model.produk = args.parent
    }

    def hitungNilaiInventory = {
        LocalDate sampaiTanggal = model.tanggalSearch.plusDays(1)
        model.nilaiInventory = labaRugiService.hitungInventory(model.produk, new CacheGlobal(tanggalMulai: sampaiTanggal))
        execInsideUISync {
            model.itemNilaiInventory.clear()
            model.itemNilaiInventory.addAll(model.nilaiInventory.toList())
            model.informasi = "<html>Total Qty: <b>${model.nilaiInventory.qty()}</b> - Nilai Inventory: <b>${NumberFormat.currencyInstance.format(model.nilaiInventory.nilai())}</b></html>"
        }
    }

    def close = {
        execInsideUISync { SwingUtilities.getWindowAncestor(view.mainPanel)?.dispose() }
    }

}