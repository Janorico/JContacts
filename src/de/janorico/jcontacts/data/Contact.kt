/*
 * JContacts is a simple contact software, written in Kotlin.
 * Copyright (C) 2023 Janosch Lion
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.janorico.jcontacts.data

import de.janorico.jcontacts.notNull

data class Contact(
    var firstName: String,
    var lastName: String,
    var birthday: Birthday?,
    var job: String?,
    var handyNumber: String?,
    var number: String?,
    var numberWork: String?,
    var email: String?,
    var group: String?,
) : Comparable<Contact> {
    override fun compareTo(other: Contact): Int = when (UDM.data.settings.sortBy) {
        SortBy.FIRST_NAME -> this.firstName.compareTo(other.firstName, true)
        SortBy.LAST_NAME -> this.lastName.compareTo(other.lastName, true)
        SortBy.GROUP -> this.group.notNull(other.group, 0) { otherGroup: String -> this.compareTo(otherGroup) }
        else -> 0
    }
}

data class Birthday(val day: Int, val month: Int, val year: Int, val hour: Int? = null, val minute: Int? = null)

fun Birthday?.toReadableString(): String = if (this == null) "none" else {
    if (hour == null || minute == null) "$day.$month.$year"
    else "$day.$month.$year $hour:$minute"
}

fun Birthday?.toReadableNullableString(): String? = this?.toReadableString()
