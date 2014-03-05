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



package domain

import groovy.transform.Canonical

import javax.persistence.Embeddable
import javax.validation.constraints.Digits
import javax.validation.constraints.NotNull
import java.text.NumberFormat

@Embeddable @Canonical
class Diskon {

    @Digits(integer=3, fraction=3)
    BigDecimal potonganPersen

    @Digits(integer=12, fraction=2)
    BigDecimal potonganLangsung

    BigDecimal hasilDiskon(BigDecimal nilai) {
        nilai - jumlahDiskon(nilai)
    }

    BigDecimal jumlahDiskon(BigDecimal nilai) {
        BigDecimal hasil = 0
        if (potonganPersen > 0) {
            hasil += (potonganPersen/100) * nilai
        }
        if (potonganLangsung) {
            hasil += potonganLangsung
        }
        hasil
    }

    String toString() {
        List result = []
        if (potonganPersen > 0) {
            result << "${NumberFormat.numberInstance.format(potonganPersen.doubleValue())} %"
        }
        if (potonganLangsung) {
            result << NumberFormat.numberInstance.format(potonganLangsung.doubleValue())
        }
        result.join(' + ')
    }

    int compareTo(Object o) {
        if (!o) return -1
        if (o instanceof Diskon) {
            return (potonganPersen?:0 - o?.potonganPersen?:0) + (potonganLangsung?:0 - o?.potonganLangsung?:0)
        } else {
            return -1
        }
    }

}
