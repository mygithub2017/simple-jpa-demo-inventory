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
package domain.general

import groovy.transform.*
import javax.persistence.*
import org.hibernate.annotations.Type
import javax.validation.constraints.*
import org.joda.time.*

@MappedSuperclass @Canonical
abstract class ItemPeriodik {

    @NotNull @Type(type="org.jadira.usertype.dateandtime.joda.PersistentLocalDate")
    LocalDate tanggal

    @NotNull
    Long jumlah

    @Size(min=3, max=255)
    String keterangan

    Long saldo

    abstract long delta()

}

