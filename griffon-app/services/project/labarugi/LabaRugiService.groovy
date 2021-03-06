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

import domain.faktur.Faktur
import domain.faktur.ItemFaktur
import domain.faktur.KRITERIA_PEMBAYARAN
import domain.inventory.ItemPenyesuaian
import domain.inventory.ItemStok
import domain.inventory.PenyesuaianStok
import domain.inventory.Produk
import domain.inventory.Transfer
import domain.labarugi.CacheGlobal
import domain.labarugi.HargaPokokPenjualan
import domain.labarugi.ItemNilaiInventory
import domain.labarugi.JENIS_KATEGORI_KAS
import domain.labarugi.KATEGORI_SISTEM
import domain.labarugi.Kas
import domain.labarugi.KategoriKas
import domain.labarugi.NilaiInventory
import domain.pembelian.PurchaseOrder
import domain.penjualan.FakturJual
import domain.penjualan.FakturJualEceran
import domain.penjualan.FakturJualOlehSales
import domain.penjualan.PencairanPoin
import domain.penjualan.ReturFaktur
import domain.retur.ReturJual
import laporan.ItemLabaRugi
import laporan.NilaiInventoryProduk
import org.joda.time.LocalDate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import project.inventory.GudangRepository
import project.inventory.ProdukRepository
import simplejpa.transaction.Transaction
import static project.labarugi.KategoriKasRepository.KATEGORI_LAIN
import static project.labarugi.KategoriKasRepository.KATEGORI_TUKAR_BARANG

@Transaction
class LabaRugiService {

    final Logger log = LoggerFactory.getLogger(LabaRugiService)

    ProdukRepository produkRepository
    GudangRepository gudangRepository
    KategoriKasRepository kategoriKasRepository
    KasRepository kasRepository

    @SuppressWarnings("GroovyUnusedDeclaration")
    void serviceInit() {
        // Buat kategori pendapatan tukar barang bila perlu
        if (!kategoriKasRepository.getKategoriSistem(KATEGORI_SISTEM.PENDAPATAN_TUKAR_BARANG)) {
            kategoriKasRepository.buat(new KategoriKas(KATEGORI_TUKAR_BARANG, JENIS_KATEGORI_KAS.PENDAPATAN, true))
        }
        if (!kategoriKasRepository.getKategoriSistem(KATEGORI_SISTEM.PENGELUARAN_TUKAR_BARANG)) {
            kategoriKasRepository.buat(new KategoriKas(KATEGORI_TUKAR_BARANG, JENIS_KATEGORI_KAS.PENGELUARAN, true))
        }

        // Buat kategori lain-lain bila perlu
        if (!kategoriKasRepository.getKategoriSistem(KATEGORI_SISTEM.PENDAPATAN_LAIN)) {
            kategoriKasRepository.buat(new KategoriKas(KATEGORI_LAIN, JENIS_KATEGORI_KAS.PENDAPATAN, true))
        }
        if (!kategoriKasRepository.getKategoriSistem(KATEGORI_SISTEM.PENGELUARAN_LAIN)) {
            kategoriKasRepository.buat(new KategoriKas(KATEGORI_LAIN, JENIS_KATEGORI_KAS.PENGELUARAN, true))
        }
    }

    NilaiInventory hitungInventory(Produk produk, CacheGlobal cacheGlobal = new CacheGlobal()) {
        int qtyTerakhir = cacheGlobal.cariQtyTerakhir(produk)
        NilaiInventory nilaiInventory = new NilaiInventory(produk: produk)
        if (qtyTerakhir > 0) {
            List daftarItemStok = cacheGlobal.cariPenerimaan(produk)
            boolean selesai = false
            int i, qty
            BigDecimal harga
            for (i=0; i<daftarItemStok.size(); i++) {
                ItemStok itemStok = daftarItemStok[i]
                if ((nilaiInventory.qty() + itemStok.jumlah) >= qtyTerakhir) {
                    qty = qtyTerakhir - nilaiInventory.qty()
                    harga = cariHarga(produk, itemStok)
                    selesai = true
                } else {
                    qty = itemStok.jumlah
                    harga = cariHarga(produk, itemStok)
                }
                nilaiInventory.tambah(itemStok, qty, harga)
                if (selesai) break
            }

            // Bila ada inventory dengan nilai kosong di baris pertama, maka cari harga beli terakhir yang paling mendekati.
            // Hal ini perlu dilakukan karena penambahan bisa disebabkan oleh invers hapus atau retur.
            if (!nilaiInventory.toList().empty && (nilaiInventory.toList()[0].harga == null)) {
                ItemNilaiInventory itemPertama = nilaiInventory.toList()[0]
                selesai = false
                for(;i<daftarItemStok.size(); i++) {
                    ItemStok itemStok = daftarItemStok[i]
                    harga = cariHarga(produk, itemStok)
                    if (harga) {
                        itemPertama.harga = harga
                        selesai = true
                    }
                    if (selesai) break
                }
            }
        }

        nilaiInventory
    }

    BigDecimal cariHarga(Produk produk, ItemStok itemStok) {
        if (itemStok.referensiStok?.classFinance == PurchaseOrder.simpleName) {
            PurchaseOrder po = findPurchaseOrderByNomor(itemStok.referensiStok.nomorFinance)
            if (po) {
                Faktur f = po.fakturBeli ?: po
                for (ItemFaktur i : f.listItemFaktur) {
                    if (i.produk == produk) {
                        return i.diskon?.hasil(i.harga) ?: i.harga
                    }
                }
            }
        } else if (itemStok.referensiStok?.classGudang == PenyesuaianStok.simpleName) {
            PenyesuaianStok ps = findPenyesuaianStokByNomor(itemStok.referensiStok.nomorGudang)
            if (ps) {
                for (ItemPenyesuaian i : ps.items) {
                    if (i.produk == produk) {
                        return i.harga ?: null
                    }
                }
            }
        }
        log.warn "Tidak menemukan referensi harga ${produk.nama} untuk ${itemStok}!"
        return null
    }

    List hitungPenjualan(LocalDate tanggalMulai, LocalDate tanggalSelesai) {
        BigDecimal penjualanSales = 0, penjualanEceran = 0, potonganPiutangReturFaktur = 0,
                   potonganPiutangReturJual = 0, potonganPiutangPoin = 0, potonganPiutangLain = 0
        List<FakturJual> fakturJuals = findAllFakturJualByTanggalBetween(tanggalMulai, tanggalSelesai)
        for (FakturJual fakturJual: fakturJuals) {
            if (fakturJual instanceof FakturJualOlehSales) {
                penjualanSales += fakturJual.nilaiPenjualan()
                if (fakturJual.piutang) {
                    BigDecimal nilaiReturFaktur = fakturJual.piutang.jumlahPotongan(ReturFaktur.simpleName)
                    BigDecimal nilaiReturJual = fakturJual.piutang.jumlahPotongan(ReturJual.simpleName)
                    BigDecimal nilaiPencairanPoin = fakturJual.piutang.jumlahPotongan(PencairanPoin.simpleName)
                    potonganPiutangReturFaktur += nilaiReturFaktur
                    potonganPiutangReturJual += nilaiReturJual
                    potonganPiutangPoin += nilaiPencairanPoin
                    potonganPiutangLain += (fakturJual.piutang.jumlahDibayar(KRITERIA_PEMBAYARAN.HANYA_POTONGAN) -
                        nilaiReturFaktur - nilaiReturJual - nilaiPencairanPoin)
                }
            } else if (fakturJual instanceof FakturJualEceran) {
                penjualanEceran += fakturJual.nilaiPenjualan()
            }
        }
        [penjualanSales, penjualanEceran, potonganPiutangReturFaktur, potonganPiutangReturJual, potonganPiutangPoin, potonganPiutangLain]
    }

    BigDecimal hitungPotonganHutang(LocalDate tanggalMulai, LocalDate tanggalSelesai) {
        BigDecimal hasil = 0
        List<PurchaseOrder> purchaseOrders = findAllPurchaseOrderByTanggalBetween(tanggalMulai, tanggalSelesai)
        for (PurchaseOrder po: purchaseOrders) {
            if (po.fakturBeli?.hutang) {
                hasil += po.fakturBeli.hutang.jumlahDibayar(KRITERIA_PEMBAYARAN.HANYA_POTONGAN)
            }
        }
        hasil
    }

    List hitungHPP(LocalDate tanggalMulai, LocalDate tanggalSelesai) {
        BigDecimal ongkosKirim = 0
        HargaPokokPenjualan hpp
        CacheGlobal cacheGlobal = new CacheGlobal()
        cacheGlobal.perbaharui(tanggalMulai, tanggalSelesai)
        List<Produk> produks = findAllProduk()
        for (Produk produk: produks) {
            NilaiInventoryProduk info = new NilaiInventoryProduk()
            if (!hpp) {
                hpp = hitungHPP(produk, info, cacheGlobal)
            } else {
                hpp.tambah(hitungHPP(produk, info, cacheGlobal))
            }
            ongkosKirim += ((info.qtyPenjualan?:0) * (produk.ongkosKirimBeli?:0))
        }
        [hpp, ongkosKirim]
    }

    HargaPokokPenjualan hitungHPP(Produk produk, NilaiInventoryProduk informasi = null, CacheGlobal cacheGlobal = new CacheGlobal()) {
        NilaiInventory nilaiInventory = hitungInventory(produk, cacheGlobal)
        HargaPokokPenjualan hasil = new HargaPokokPenjualan(nilaiInventory)
        if (informasi) {
            informasi.produk = produk
            informasi.nilaiAwal = nilaiInventory.nilai()
        }
        List<ItemStok> itemStoks = cacheGlobal.cariPerubahan(produk)
        for (ItemStok itemStok: itemStoks) {
            // Abaikan transfer karena tidak mempengaruhi laba rugi
            if (itemStok.referensiStok?.classGudang != Transfer.simpleName) {
                if (itemStok.jumlah > 0) {
                    BigDecimal harga = cariHarga(produk, itemStok)
                    nilaiInventory.tambah(itemStok.tanggal, itemStok?.referensiStok?.pihakTerkait, itemStok.jumlah, harga)
                    if (informasi) {
                        informasi.nilaiAwal += ((itemStok.jumlah?:0) * (harga?:0))
                    }
                } else {
                    hasil.tambah(itemStok.referensiStok, Math.abs(itemStok.jumlah))
                }
            }
        }
        if (informasi) {
            informasi.nilaiHPP = hasil.totalNilai()
            informasi.nilaiAkhir = informasi.nilaiAwal - informasi.nilaiHPP
            informasi.qtyPenjualan = hasil.totalQty()
        }
        hasil
    }

    List<ItemLabaRugi> laporanLabaRugi(LocalDate tanggalMulai, LocalDate tanggalSelesai) {
        List<ItemLabaRugi> hasil = []
        def (penjualanSales, penjualanEceran, potonganPiutangReturFaktur, potonganPiutangReturJual, potonganPiutangPoin, potonganPiutangLain) = hitungPenjualan(tanggalMulai, tanggalSelesai)
        def (hpp, ongkosKirimBeli) = hitungHPP(tanggalMulai, tanggalSelesai)

        hasil << new ItemLabaRugi('Pendapatan Piutang Penjualan Sales (Gross)', penjualanSales, null)
        hasil << new ItemLabaRugi('Pendapatan Penjualan Eceran', penjualanEceran, null)
        hasil << new ItemLabaRugi('Pendapatan Operasional', totalPendapatan(tanggalMulai, tanggalSelesai), null)
        hasil << new ItemLabaRugi('HPP (Penjualan Sales)', null, hpp.nilaiPenjualanOlehSales)
        hasil << new ItemLabaRugi('HPP (Penjualan Eceran)', null, hpp.nilaiPenjualanEceran)
        hasil << new ItemLabaRugi('Ongkos Kirim Pembelian', null, ongkosKirimBeli)
        hasil << new ItemLabaRugi('Potongan Piutang (Retur Faktur)', null, potonganPiutangReturFaktur)
        hasil << new ItemLabaRugi('Potongan Piutang (Retur Jual)', null, potonganPiutangReturJual)
        hasil << new ItemLabaRugi('Potongan Piutang (Pencairan Poin)', null, potonganPiutangPoin)
        hasil << new ItemLabaRugi('Potongan Piutang (Lain-Lain)', null, potonganPiutangLain)
        hasil << new ItemLabaRugi('Pengeluaran Operasional', null, totalPengeluaran(tanggalMulai, tanggalSelesai))

        hasil
    }

    List<ItemLabaRugi> laporanLabaRugiDetail(LocalDate tanggalMulai, LocalDate tanggalSelesai) {
        List<ItemLabaRugi> hasil = []
        def (penjualanSales, penjualanEceran, potonganPiutangReturFaktur, potonganPiutangReturJual, potonganPiutangPoin, potonganPiutangLain) = hitungPenjualan(tanggalMulai, tanggalSelesai)
        def (hpp, ongkosKirimBeli) = hitungHPP(tanggalMulai, tanggalSelesai)
        def potonganHutang = hitungPotonganHutang(tanggalMulai, tanggalSelesai)

        hasil << new ItemLabaRugi('Pendapatan Piutang Penjualan Sales (Gross)', penjualanSales, null)
        hasil << new ItemLabaRugi('Pendapatan Penjualan Eceran', penjualanEceran, null)
        hasil << new ItemLabaRugi('Pendapatan Operasional', totalPendapatan(tanggalMulai, tanggalSelesai), null)
        hasil << new ItemLabaRugi('Potongan Hutang', potonganHutang, null)
        hasil << new ItemLabaRugi('HPP (Penjualan Sales)', null, hpp.nilaiPenjualanOlehSales)
        hasil << new ItemLabaRugi('HPP (Penjualan Eceran)', null, hpp.nilaiPenjualanEceran)
        hasil << new ItemLabaRugi('HPP (Retur Sales)', null, hpp.nilaiReturSales)
        hasil << new ItemLabaRugi('HPP (Retur Eceran)', null, hpp.nilaiReturEceran)
        hasil << new ItemLabaRugi('HPP (Penyesuaian)', null, hpp.nilaiPenyesuaianStok)
        hasil << new ItemLabaRugi('HPP (Lain-Lain)', null, hpp.nilaiLain)
        hasil << new ItemLabaRugi('Ongkos Kirim Pembelian', null, ongkosKirimBeli)
        hasil << new ItemLabaRugi('Potongan Piutang (Retur Faktur)', null, potonganPiutangReturFaktur)
        hasil << new ItemLabaRugi('Potongan Piutang (Retur Jual)', null, potonganPiutangReturJual)
        hasil << new ItemLabaRugi('Potongan Piutang (Pencairan Poin)', null, potonganPiutangPoin)
        hasil << new ItemLabaRugi('Potongan Piutang (Lain-Lain)', null, potonganPiutangLain)
        hasil << new ItemLabaRugi('Pengeluaran Operasional', null, totalPengeluaran(tanggalMulai, tanggalSelesai))

        hasil
    }

    long totalPendapatan(LocalDate tanggalMulai, LocalDate tanggalSelesai) {
        long hasil = 0
        for (Kas kas: kasRepository.cariUntukLabaRugi()) {
            hasil += kas.jumlah(tanggalMulai, tanggalSelesai, JENIS_KATEGORI_KAS.PENDAPATAN, true)
        }
        hasil
    }

    long totalPengeluaran(LocalDate tanggalMulai, LocalDate tanggalSelesai) {
        long hasil = 0
        for (Kas kas: kasRepository.cariUntukLabaRugi()) {
            hasil += kas.jumlah(tanggalMulai, tanggalSelesai, JENIS_KATEGORI_KAS.PENGELUARAN, true)
        }
        hasil
    }

}
