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
package domain.inventory

import domain.faktur.Faktur
import domain.validation.TanpaGudang
import groovy.transform.Canonical
import org.apache.tools.ant.taskdefs.condition.Not
import org.hibernate.annotations.Type
import org.hibernate.validator.constraints.NotBlank
import org.hibernate.validator.constraints.NotEmpty
import org.joda.time.LocalDate
import simplejpa.DomainClass

import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.Inheritance
import javax.persistence.InheritanceType
import javax.persistence.ManyToOne
import javax.persistence.NamedAttributeNode
import javax.persistence.NamedEntityGraph
import javax.persistence.NamedSubgraph
import javax.persistence.OrderColumn
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size
import javax.validation.groups.Default

@NamedEntityGraph(name='PenerimaanBarang.Complete', attributeNodes = [
    @NamedAttributeNode(value='listItemBarang')
])
@DomainClass @Entity @Canonical(excludes='listItemBarang')
abstract class DaftarBarang {

    @NotBlank(groups=[Default,TanpaGudang]) @Size(min=2, max=100, groups=[Default,TanpaGudang])
    String nomor

    @NotNull(groups=[Default,TanpaGudang]) @Type(type="org.jadira.usertype.dateandtime.joda.PersistentLocalDate")
    LocalDate tanggal

    @Size(min=2, max=200, groups=[Default,TanpaGudang])
    String keterangan

    @NotNull(groups=[Default]) @ManyToOne
    Gudang gudang

    @ManyToOne
    Faktur faktur

    @ElementCollection @OrderColumn @NotEmpty(groups=[Default,TanpaGudang])
    List<ItemBarang> listItemBarang = []

    abstract int faktor()

    void tambah(ItemBarang itemBarang) {
        listItemBarang << itemBarang
    }

    int jumlah() {
        listItemBarang.sum { it.jumlah }?: 0
    }

    int jumlah(Produk produk) {
        normalisasi().find { it.produk == produk}?.jumlah?: 0
    }

    List<ItemBarang> normalisasi() {
        List hasil = []
        listItemBarang.groupBy { it.produk }.each { k, v ->
            hasil << new ItemBarang(k, v.sum { it.jumlah })
        }
        hasil
    }

}
