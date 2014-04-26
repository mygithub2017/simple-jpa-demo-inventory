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

import domain.penjualan.StatusFakturJual

import static ca.odell.glazedlists.gui.AbstractTableComparatorChooser.*
import static javax.swing.SwingConstants.*
import net.miginfocom.swing.MigLayout
import org.joda.time.*
import java.awt.*
import org.jdesktop.swingx.prompt.PromptSupport

actions {
    action(id: 'showItemFaktur', name: 'Klik Disini Untuk Melihat Atau Mengisi Item Faktur Jual...', closure: controller.showItemFaktur)
}

application(title: 'Faktur Jual Eceran',
        preferredSize: [520, 340],
        pack: true,
        locationByPlatform: true,
        iconImage: imageIcon('/griffon-icon-48x48.png').image,
        iconImages: [imageIcon('/griffon-icon-48x48.png').image,
                     imageIcon('/griffon-icon-32x32.png').image,
                     imageIcon('/griffon-icon-16x16.png').image]) {

    panel(id: 'mainPanel') {
        borderLayout()

        panel(constraints: PAGE_START) {
            flowLayout(alignment: FlowLayout.LEADING)
            dateTimePicker(id: 'tanggalMulaiSearch', localDate: bind('tanggalMulaiSearch', target: model, mutual: true), timeVisible: false)
            label(' s/d ')
            dateTimePicker(id: 'tanggalSelesaiSearch', localDate: bind('tanggalSelesaiSearch', target: model, mutual: true), timeVisible: false)
            comboBox(id: 'statusSearch', model: model.statusSearch)
            textField(id: 'nomorSearch', columns: 10, text: bind('nomorSearch', target: model, mutual: true), actionPerformed: controller.search)
            textField(id: 'namaPembeliSearch', columns: 10, text: bind('namaPembeliSearch', target: model, mutual: true), actionPerformed: controller.search)
            button(app.getMessage('simplejpa.search.label'), actionPerformed: controller.search)
        }


        scrollPane(constraints: CENTER) {
            glazedTable(id: 'table', list: model.fakturJualEceranList, sortingStrategy: SINGLE_COLUMN, onValueChanged: controller.tableSelectionChanged,
                    doubleClickAction: showItemFaktur, enterKeyAction: showItemFaktur) {
                glazedColumn(name: '', property: 'deleted', width: 20) {
                    templateRenderer(exp: { it == 'Y'? 'D': ''})
                }
                glazedColumn(name: 'Nomor Faktur', property: 'nomor', width: 140)
                glazedColumn(name: 'Tanggal', property: 'tanggal', width: 100) {
                    templateRenderer(exp: { it?.toString('dd-MM-yyyy') })
                }
                glazedColumn(name: 'Name Pembeli', property: 'namaPembeli')
                glazedColumn(name: 'Status', property: 'status')
                glazedColumn(name: 'Keterangan', property: 'keterangan')
                glazedColumn(name: 'Diskon', property: 'diskon', columnClass: Integer, visible: bind { model.showNilaiUang })
                glazedColumn(name: 'Jumlah Diskon', visible: bind { model.showNilaiUang }, expression: { it.jumlahDiskon() }, columnClass: Integer) {
                    templateRenderer(exp: { !it ? '-' : currencyFormat(it) }, horizontalAlignment: RIGHT)
                }
                glazedColumn(name: 'Total', expression: { it.total() }, columnClass: Integer, visible: bind { model.showNilaiUang }) {
                    templateRenderer(exp: { currencyFormat(it) }, horizontalAlignment: RIGHT)
                }
            }
        }

        panel(constraints: PAGE_END) {
            borderLayout()
            panel(id: "form", layout: new MigLayout('', '[right][left][left,grow]', ''), constraints: CENTER, focusCycleRoot: true, visible: bind { model.allowAddFakturJual }) {
                label('Nomor:')
                label(id: 'nomor', text: bind('nomor', source: model), errorPath: 'nomor')
                errorLabel(path: 'nomor', constraints: 'wrap')
                label('Tanggal:')
                dateTimePicker(id: 'tanggal', localDate: bind('tanggal', target: model, mutual: true), errorPath: 'tanggal', timeVisible: false)
                errorLabel(path: 'tanggal', constraints: 'wrap')
                label('Nama Pembeli:')
                textField(id: 'namaPembeli', columns: 30, text: bind('namaPembeli', target: model, mutual: true), errorPath: 'namaPembeli')
                errorLabel(path: 'namaPembeli', constraints: 'wrap')
                label('Diskon:')
                panel(layout: new FlowLayout(FlowLayout.LEADING, 0, 0)) {
                    decimalTextField(id: 'diskonPotonganPersen', columns: 5, bindTo: 'diskonPotonganPersen', errorPath: 'diskonPotonganPersen')
                    label('% dan Potongan Langsung Rp')
                    decimalTextField(id: 'diskonPotonganLangsung', columns: 20, bindTo: 'diskonPotonganLangsung', errorPath: 'diskonPotonganLangsung')
                }
                errorLabel(path: 'diskon', constraints: 'wrap')
                label('Isi:')
                panel {
                    label(text: bind { model.informasi })
                    button(id: 'listItemFaktur', action: showItemFaktur, errorPath: 'listItemFaktur')
                }
                errorLabel(path: 'listItemFaktur', constraints: 'wrap')
                label('Keterangan:')
                textField(id: 'keterangan', columns: 60, text: bind('keterangan', target: model, mutual: true), errorPath: 'keterangan')
                errorLabel(path: 'keterangan', constraints: 'wrap')
            }

            panel(constraints: PAGE_END) {
                flowLayout(alignment: FlowLayout.LEADING)
                button(app.getMessage("simplejpa.dialog.save.button"), visible: bind {
                    model.allowAddFakturJual
                }, actionPerformed: {
                    if (model.id != null) {
                        if (JOptionPane.showConfirmDialog(mainPanel, app.getMessage("simplejpa.dialog.update.message"),
                                app.getMessage("simplejpa.dialog.update.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
                            return
                        }
                    }
                    controller.save()
                    form.getFocusTraversalPolicy().getFirstComponent(form).requestFocusInWindow()
                })
                button('Antar', visible: bind('isRowSelected', source: table, converter: { it && model.showPenerimaan }),
                    actionPerformed: controller.antar)
                button('Batal Antar', visible: bind('isRowSelected', source: table, converter: { it && model.showPenerimaan && (model.status == StatusFakturJual.DIANTAR) }),
                    actionPerformed: controller.batalAntar)
                button('Barang Diterima Dan Telah Dibayar', visible: bind('isRowSelected', source: table, converter: { it && model.showFakturJual && (model.status == StatusFakturJual.DIANTAR) }),
                    actionPerformed: controller.bayar)
                mvcPopupButton('Cetak', mvcGroup: 'previewFaktur', args: {[
                    dataSource: view.table.selectionModel.selected[0], fileLaporan: 'fakturJualEceran'
                ]}, dialogProperties: [title: 'Preview Faktur Jual', size: new Dimension(820,600)],
                    visible: bind('isRowSelected', source: table, converter: { it && model.showFakturJual } ))
                button(app.getMessage("simplejpa.dialog.cancel.button"), visible: bind {
                    table.isRowSelected
                }, actionPerformed: controller.clear)
                button(app.getMessage("simplejpa.dialog.delete.button"), visible: bind('isRowSelected', source: table, converter: {
                    it && model.allowAddFakturJual
                }), actionPerformed: {
                    if (JOptionPane.showConfirmDialog(mainPanel, app.getMessage("simplejpa.dialog.delete.message"),
                            app.getMessage("simplejpa.dialog.delete.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                        controller.delete()
                    }
                })
            }
        }
    }
}

PromptSupport.setPrompt("Nama Pembeli...", namaPembeliSearch)
PromptSupport.setPrompt("Nomor Faktur...", nomorSearch)