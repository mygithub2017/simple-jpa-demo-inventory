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

import ast.NeedSupervisorPassword
import domain.exception.DataTidakBolehDiubah
import domain.exception.MelebihiBatasKredit
import domain.exception.StokTidakCukup
import domain.faktur.Diskon
import domain.penjualan.*
import domain.validation.InputPenjualanOlehSales
import org.joda.time.LocalDate
import project.penjualan.FakturJualOlehSalesModel
import simplejpa.swing.DialogUtils

import javax.swing.JOptionPane
import javax.swing.event.ListSelectionEvent
import javax.validation.groups.Default
import com.google.common.base.Strings
import domain.exception.DataDuplikat
import domain.Container

import java.awt.Dimension
import java.text.NumberFormat

class FakturJualOlehSalesController {

    FakturJualOlehSalesModel model
    def view

    FakturJualRepository fakturJualRepository

    void mvcGroupInit(Map args) {
        fakturJualRepository = Container.app.fakturJualRepository
        init()
        search()
    }

    def init = {
        Container.app.nomorService.refreshAll()
        execInsideUISync {
            model.konsumenList.clear()
            model.salesList.clear()
        }
        List konsumen = fakturJualRepository.findAllKonsumen()
        List sales  = fakturJualRepository.findAllSales()
        execInsideUISync {
            model.konsumenList.addAll(konsumen)
            model.salesList.addAll(sales)
            model.nomor = Container.app.nomorService.getCalonNomor(NomorService.TIPE.FAKTUR_JUAL)
            model.tanggalMulaiSearch = LocalDate.now().minusMonths(1)
            model.tanggalSelesaiSearch = LocalDate.now()
            model.statusSearch.selectedItem = Container.SEMUA
        }
    }

    def search = {
        List result = fakturJualRepository.cariFakturJualOlehSales(model.tanggalMulaiSearch, model.tanggalSelesaiSearch,
            model.nomorSearch, model.salesSearch, model.konsumenSearch, model.statusSearch.selectedItem)
        execInsideUISync {
            model.fakturJualOlehSalesList.clear()
            model.fakturJualOlehSalesList.addAll(result)
        }
    }

    def save = {
        FakturJualOlehSales fakturJualOlehSales = new FakturJualOlehSales(id: model.id, nomor: model.nomor, tanggal: model.tanggal,
            keterangan: model.keterangan, diskon: new Diskon(model.diskonPotonganPersen, model.diskonPotonganLangsung),
            sales: model.sales.selectedItem, konsumen: model.konsumen.selectedItem)
        model.listItemFaktur.each { fakturJualOlehSales.tambah(it) }

        if (!fakturJualRepository.validate(fakturJualOlehSales, InputPenjualanOlehSales, model)) return

        try {
            if (fakturJualOlehSales.id == null) {
                if (!model.listBonus.empty) {
                    fakturJualRepository.buat(fakturJualOlehSales, false, model.listBonus)
                } else {
                    fakturJualRepository.buat(fakturJualOlehSales, false)
                }
                execInsideUISync {
                    model.fakturJualOlehSalesList << fakturJualOlehSales
                    view.table.changeSelection(model.fakturJualOlehSalesList.size() - 1, 0, false, false)
                    clear()
                }
            } else {
                fakturJualOlehSales = fakturJualRepository.update(fakturJualOlehSales)
                execInsideUISync {
                    view.table.selectionModel.selected[0] = fakturJualOlehSales
                    clear()
                }
            }
        } catch (DataDuplikat ex) {
            model.errors['nomor'] = app.getMessage("simplejpa.error.alreadyExist.message")
        } catch (StokTidakCukup ex) {
            model.errors['listItemFaktur'] = ex.message
        } catch (MelebihiBatasKredit ex) {
            model.errors['konsumen'] = ex.message
        } catch (DataTidakBolehDiubah ex) {
            JOptionPane.showMessageDialog(view.mainPanel, 'Faktur jual tidak boleh diubah karena sudah diproses!', 'Penyimpanan Gagal', JOptionPane.ERROR_MESSAGE)
        }
    }

    @NeedSupervisorPassword
    def delete = {
        try {
            FakturJualOlehSales fakturJualOlehSales = view.table.selectionModel.selected[0]
            fakturJualOlehSales = fakturJualRepository.hapus(fakturJualOlehSales)

            execInsideUISync {
                view.table.selectionModel.selected[0] = fakturJualOlehSales
                clear()
            }
        } catch (DataTidakBolehDiubah ex) {
            JOptionPane.showMessageDialog(view.mainPanel, 'Faktur jual tidak boleh diubah karena sudah diproses!', 'Penyimpanan Gagal', JOptionPane.ERROR_MESSAGE)
        }
    }

    def showBonus = {
        execInsideUISync {
            def args = [editable: false, listItemBarang: model.listBonus, allowTambahProduk: false]
            if (view.table.selectionModel.selected[0] == null) {
                args.'editable' = true
            }

            def dialogProps = [title: 'Detail Bonus', size: new Dimension(900, 420)]
            DialogUtils.showMVCGroup('itemBarangAsChild', args, app, view, dialogProps) { m, v, c ->
                model.listBonus.clear()
                model.listBonus.addAll(m.itemBarangList)
                refreshInformasi()
            }
        }
    }

    def showItemFaktur = {
        execInsideUISync {
            def args = [parent: view.table.selectionModel.selected[0], listItemFaktur: model.listItemFaktur,
                        allowTambahProduk: false, showHarga: model.showFakturJual]
            def dialogProps = [title: 'Detail Item', size: new Dimension(900, 420)]
            DialogUtils.showMVCGroup('itemFakturAsChild', args, app, view, dialogProps) { m, v, c ->
                model.listItemFaktur.clear()
                model.listItemFaktur.addAll(m.itemFakturList)
                refreshInformasi()
            }
        }
    }

    def refreshInformasi = {
        def jumlahItem = model.listItemFaktur.toArray().sum{ it.jumlah }?: 0
        def total = model.listItemFaktur.toArray().sum { it.total() }?: 0
        model.informasi = "Qty ${jumlahItem}   Total ${NumberFormat.currencyInstance.format(total)}"

        jumlahItem = model.listBonus.toArray().sum { it.jumlah }?: 0
        model.informasiBonus = "Qty ${jumlahItem}"
    }

    def clear = {
        execInsideUISync {
            model.id = null
            model.nomor = Container.app.nomorService.getCalonNomor(NomorService.TIPE.FAKTUR_JUAL)
            model.tanggal = null
            model.sales.selectedItem = null
            model.konsumen.selectedItem = null
            model.keterangan = null
            model.diskonPotonganLangsung = null
            model.diskonPotonganPersen = null
            model.status = null
            model.listItemFaktur.clear()
            model.listBonus.clear()

            model.errors.clear()
            view.table.selectionModel.clearSelection()
            refreshInformasi()
        }
    }

    def tableSelectionChanged = { ListSelectionEvent event ->
        execInsideUISync {
            if (view.table.selectionModel.isSelectionEmpty()) {
                clear()
            } else {
                FakturJualOlehSales selected = view.table.selectionModel.selected[0]
                model.errors.clear()
                model.id = selected.id
                model.nomor = selected.nomor
                model.tanggal = selected.tanggal
                model.sales.selectedItem = selected.sales
                model.konsumen.selectedItem = selected.konsumen
                model.keterangan = selected.keterangan
                model.diskonPotonganLangsung = selected.diskon?.potonganLangsung
                model.diskonPotonganPersen = selected.diskon?.potonganPersen
                model.status = selected.status
                model.listItemFaktur.clear()
                model.listItemFaktur.addAll(selected.listItemFaktur)
                model.listBonus.clear()
                if (selected.bonusPenjualan) {
                    model.listBonus.addAll(selected.bonusPenjualan.items)
                }
                refreshInformasi()
            }
        }
    }

}