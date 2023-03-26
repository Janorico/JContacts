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

import com.formdev.flatlaf.*
import com.formdev.flatlaf.themes.*
import de.janorico.jcontacts.*
import org.w3c.dom.Element
import java.io.*
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

/**
 * UserDataManager
 */
object UDM {
    var data = UserData.DEFAULT
    private val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()

    fun refresh() {
        data = load()
        when (data.settings.theme) {
            Theme.LIGHT -> FlatLightLaf.setup()
            Theme.DARK -> FlatDarkLaf.setup()
            Theme.INTELLIJ -> FlatIntelliJLaf.setup()
            Theme.DARCULA -> FlatDarculaLaf.setup()
            Theme.MAC_LIGHT -> FlatMacLightLaf.setup()
            Theme.MAC_DARK -> FlatMacDarkLaf.setup()
        }
        FlatLaf.updateUI()
    }

    private fun getFile(): File {
        val file = File(System.getenv("APPDATA") + "/Janorico/JContacts.xml")
        if (!file.isFile) {
            file.createNewFile()
            write(UserData.DEFAULT, file)
        }
        return file
    }

    fun write() {
        write(getFile())
    }

    fun write(file: File) {
        write(data, file)
    }

    private fun write(writeData: UserData, file: File) {
        // Create XML
        val doc = builder.newDocument()
        val element = doc.createElement("JContactsUserData")
        element.appendChild(doc.createElement("contacts").apply {
            for (contact in writeData.contacts) appendChild(doc.createElement("contact").apply {
                setAttribute("first-name", contact.firstName)
                setAttribute("last-name", contact.lastName)
                // Birthday
                contact.birthday.notNull B@{
                    appendChild(doc.createElement("birthday").apply {
                        setAttribute("day", this@B.day.toString())
                        setAttribute("month", this@B.month.toString())
                        setAttribute("year", this@B.year.toString())
                        this@B.hour.notNull { setAttribute("hour", this.toString()) }
                        this@B.minute.notNull { setAttribute("minute", this.toString()) }
                    })
                }
                contact.job.notNull { setAttribute("job", this) }
                contact.handyNumber.notNull { setAttribute("handy-number", this) }
                contact.number.notNull { setAttribute("number", this) }
                contact.numberWork.notNull { setAttribute("number-private", this) }
                contact.email.notNull { setAttribute("email", this) }
                contact.group.notNull { setAttribute("group", this) }
            })
        })
        element.appendChild(doc.createElement("settings").apply {
            setAttribute("theme", writeData.settings.theme.toString())
            setAttribute("title-string", writeData.settings.titleString)
            setAttribute("detail-string", writeData.settings.detailString)
            setAttribute("detail-string-enabled", writeData.settings.detailStringEnabled.toString())
            setAttribute("sort-by", writeData.settings.sortBy.toString())
        })
        if (writeData.licenseAgreed) element.setAttribute("license-agreed", "true")
        doc.appendChild(element)
        // Write
        val fos = FileOutputStream(file)
        val sr = StreamResult(fos)
        val transformer = TransformerFactory.newInstance().newTransformer()
        transformer.transform(DOMSource(doc), sr)
        fos.close()
    }

    private fun load(): UserData = load(getFile())

    fun load(file: File): UserData {
        // Read
        val fis = FileInputStream(file)
        val doc = builder.parse(fis)
        fis.close()
        // Parse
        val element = doc.documentElement
        // Contacts
        val contacts = ArrayList<Contact>()
        element.getElementsByTagName("contacts").item(0).apply {
            val children = childNodes
            for (idx in 0 until children.length) {
                val child = children.item(idx) as Element
                var birthday: Birthday? = null
                child.getElementsByTagName("birthday").apply {
                    if (this.length > 0) {
                        (this.item(0) as Element).apply {
                            birthday = Birthday(
                                this.getAttribute("day").toInt(),
                                this.getAttribute("month").toInt(),
                                this.getAttribute("year").toInt(),
                                this.getAttribute("hour").intOrNull(),
                                this.getAttribute("minute").intOrNull()
                            )
                        }
                    }
                }
                contacts.add(
                    Contact(child.getAttribute("first-name"),
                        child.getAttribute("last-name"),
                        birthday,
                        child.getAttribute("job").ifBlank { null },
                        child.getAttribute("handy-number").ifBlank { null },
                        child.getAttribute("number").ifBlank { null },
                        child.getAttribute("number-work").ifBlank { null },
                        child.getAttribute("email").ifBlank { null },
                        child.getAttribute("group").ifBlank { null })
                )
            }
        }
        var settings = UserSettings.DEFAULT
        (element.getElementsByTagName("settings").item(0) as Element).apply {
            settings = UserSettings(
                Theme.valueOf(getAttribute("theme").ifBlank { "DARK" }),
                getAttribute("title-string"),
                getAttribute("detail-string"),
                getAttribute("detail-string-enabled").toBoolean(),
                SortBy.valueOf(getAttribute("sort-by").ifBlank { "LAST_NAME" })
            )
        }
        if (settings.sortBy != SortBy.NONE) contacts.sort()
        //contacts.sortWith(AlphabeticalComparator { if (settings.sortBy == SortBy.FIRST_NAME) it.firstName else it.lastName })
        return UserData(contacts, settings, (element.hasAttribute("license-agreed") && (element.getAttribute("license-agreed") == "true")))
    }
}

data class UserData(val contacts: ArrayList<Contact>, var settings: UserSettings, var licenseAgreed: Boolean) {
    companion object {
        val DEFAULT = UserData(arrayListOf(Contact("Janorico", "", null, null, null, null, null, "janorico@posteo.de", null)), UserSettings.DEFAULT, false)
    }

    fun getGroups(): Array<String> {
        val list = ArrayList<String>()
        for (contact in contacts) {
            val group = contact.group
            if (group != null && !list.contains(group)) {
                list.add(group)
            }
        }
        return list.toTypedArray()
    }
}

data class UserSettings(val theme: Theme, val titleString: String, val detailString: String, val detailStringEnabled: Boolean, val sortBy: SortBy) {
    companion object {
        const val FIRST_NAME_PLACEHOLDER = "\$fn"
        const val LAST_NAME_PLACEHOLDER = "\$ln"
        const val BIRTHDAY_PLACEHOLDER = "\$b"
        const val JOB_PLACEHOLDER = "\$j"
        const val HANDY_NUMBER_PLACEHOLDER = "\$hn"
        const val NUMBER_PLACEHOLDER = "\$n"
        const val NUMBER_WORK_PLACEHOLDER = "\$nw"
        const val EMAIL_PLACEHOLDER = "\$e"
        const val GROUP_PLACEHOLDER = "\$g"
        val DEFAULT = UserSettings(Theme.DARK, "\$fn \$ln", "Phone: \$n  Handy: \$hn", true, SortBy.LAST_NAME)
    }
}

enum class Theme {
    LIGHT, DARK, INTELLIJ, DARCULA, MAC_LIGHT, MAC_DARK
}

enum class SortBy {
    NONE, FIRST_NAME, LAST_NAME
}
