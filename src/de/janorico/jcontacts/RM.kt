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

import de.janorico.jcontacts.gui.Dialogs
import java.awt.image.BufferedImage
import java.io.IOException
import java.net.URL
import javax.imageio.ImageIO
import javax.swing.ImageIcon

object RM {
    fun getImage(name: String): BufferedImage = ImageIO.read(getURL("images/$name.png"))
    fun getIcon(name: String): ImageIcon = ImageIcon(getURL("images/$name.png"))
    fun getURL(name: String): URL = Dialogs::class.java.classLoader.getResource(name) ?: throw IOException("Cant find resource \"$name\"!")
}
