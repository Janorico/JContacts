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

package de.janorico.jcontacts.gui

import de.janorico.jcontacts.*
import de.janorico.jcontacts.data.*
import java.awt.*
import javax.swing.*

class ContactRenderer : ListCellRenderer<Contact> {
    companion object {
        private val avatarIcon = RM.getIcon("Avatar32")
    }

    override fun getListCellRendererComponent(list: JList<out Contact>, value: Contact, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
        return JPanel(BorderLayout()).apply {
            toolTipText = """
                First name: ${value.firstName}
                Last name: ${value.lastName}
                Birthday: ${value.birthday.toReadableString()}
                Job: ${value.job.notNull()}
                Handy number: ${value.handyNumber.notNull()}
                Number: ${value.number.notNull()}
                Email: ${value.email.notNull()}
            """.trimIndent()
            add(JLabel(avatarIcon), BorderLayout.WEST)
            val detail = UDM.data.settings.detailStringEnabled
            val panel = JPanel(GridLayout(if (detail) 2 else 1, 1)).apply {
                add(JLabel(placeholder(UDM.data.settings.titleString, value)).apply { font = Font(font.name, Font.BOLD, font.size) })
                if (detail) add(JLabel(placeholder(UDM.data.settings.detailString, value)))
            }
            if (isSelected) {
                this.foreground = list.selectionForeground
                this.background = list.selectionBackground
                panel.foreground = list.selectionForeground
                panel.background = list.selectionBackground
            } else {
                this.foreground = list.foreground
                this.background = list.background
                panel.foreground = list.foreground
                panel.background = list.background
            }
            add(panel)
        }
    }

    private fun placeholder(placeholder: String, contact: Contact): String =
        placeholder.replace(UserSettings.FIRST_NAME_PLACEHOLDER, contact.firstName).replace(UserSettings.LAST_NAME_PLACEHOLDER, contact.lastName)
            .replace(UserSettings.BIRTHDAY_PLACEHOLDER, contact.birthday.toReadableString()).replace(UserSettings.JOB_PLACEHOLDER, contact.job.notNull())
            .replace(UserSettings.HANDY_NUMBER_PLACEHOLDER, contact.handyNumber.notNull()).replace(UserSettings.NUMBER_PLACEHOLDER, contact.number.notNull())
            .replace(UserSettings.NUMBER_WORK_PLACEHOLDER, contact.numberWork.notNull()).replace(UserSettings.EMAIL_PLACEHOLDER, contact.email.notNull())
}
