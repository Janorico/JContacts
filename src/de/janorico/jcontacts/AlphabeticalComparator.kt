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

package de.janorico.jcontacts

import kotlin.math.min

class AlphabeticalComparator<T>(val objectToString: (o: T) -> String) : Comparator<T> {
    private val alphabet = arrayOf('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z')

    override fun compare(o1: T, o2: T): Int {
        // Falls s1 > s2 dann positiv
        val s1 = objectToString(o1)
        val s2 = objectToString(o2)
        for (i in 0 until min(s1.length, s2.length) - 1)
            if (s1[i] != s2[i]) return alphabet.indexOf(s1[i]) - alphabet.indexOf(s2[i])
        return s1.length - s2.length
    }
}
