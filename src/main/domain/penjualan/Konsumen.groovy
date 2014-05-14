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
package domain.penjualan

import domain.faktur.Diskon
import domain.inventory.Produk
import groovy.transform.*
import simplejpa.DomainClass
import javax.persistence.*
import javax.validation.constraints.*
import org.hibernate.validator.constraints.*

@NamedEntityGraphs([
    @NamedEntityGraph(name='Konsumen.Complete', attributeNodes = [
        @NamedAttributeNode('listFakturBelumLunas'),
        @NamedAttributeNode('hargaTerakhir')
    ], subgraphs = [
        @NamedSubgraph(name='faktur', attributeNodes = [
                @NamedAttributeNode('listItemFaktur')
        ])
    ]),
    @NamedEntityGraph(name='Konsumen.FakturBelumLunas', attributeNodes = [
        @NamedAttributeNode(value='listFakturBelumLunas', subgraph='faktur'),
    ], subgraphs = [
        @NamedSubgraph(name='faktur', attributeNodes = [
            @NamedAttributeNode('listItemFaktur')
        ])
    ]),
])
@DomainClass @Entity @Canonical(excludes='listFakturBelumLunas,hargaTerakhir')
class Konsumen {

    @NotEmpty @Size(min=2, max=100)
    String nama

    @Size(min=2, max=50)
    String nomorTelepon

    @Size(min=3, max=200)
    String alamat

    @NotNull @ManyToOne
    Region region

    @NotNull @ManyToOne
    Sales sales

    @NotNull
    BigDecimal creditLimit = BigDecimal.ZERO

    @NotNull
    BigDecimal creditTerpakai = BigDecimal.ZERO

    @OneToMany(cascade=CascadeType.ALL, orphanRemoval=true) @JoinTable @OrderColumn(name='FAKTUR_ORDER')
    List<FakturJualOlehSales> listFakturBelumLunas = []

    @ElementCollection
    Map<Produk, BigDecimal> hargaTerakhir = [:]

    public BigDecimal jumlahPiutang() {
        listFakturBelumLunas.sum { it.total() }?: 0
    }

    public boolean adaTagihanJatuhTempo() {
        listFakturBelumLunas.any { it.sudahJatuhTempo() }
    }

    public boolean bolehKredit(BigDecimal pengajuan) {
        if (adaTagihanJatuhTempo()) return false
        if ((jumlahPiutang() + pengajuan) > creditLimit) return false
        true
    }

    public void tambahFakturBelumLunas(FakturJualOlehSales faktur) {
        if (!listFakturBelumLunas.contains(faktur)) {
            listFakturBelumLunas << faktur
            creditTerpakai += faktur.total()
        }
    }

    public void hapusFakturBelumLunas(FakturJualOlehSales faktur) {
        listFakturBelumLunas.remove(faktur)
        creditTerpakai -= faktur.total()
    }

    public BigDecimal getRatioPenggunaanCredit() {
        if (creditLimit==0) {
            return 0
        } else {
            return creditTerpakai / creditLimit
        }
    }

}

