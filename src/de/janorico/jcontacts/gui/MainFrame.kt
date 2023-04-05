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

import com.formdev.flatlaf.FlatClientProperties
import com.formdev.flatlaf.icons.*
import de.janorico.jcontacts.*
import de.janorico.jcontacts.data.*
import de.janorico.jgl.JGL
import de.janorico.jgl.helpers.*
import de.janorico.jgl.helpers.Button
import de.janorico.jgl.helpers.Dialog
import de.janorico.jgl.helpers.MenuItem
import java.awt.*
import java.awt.event.*
import java.time.LocalDateTime
import javax.swing.*
import kotlin.system.exitProcess

class MainFrame : JFrame() {
    companion object {
        private val avatarIcon = RM.getIcon("Avatar128")
    }

    private val list = JList(UDM.data.contacts.toTypedArray())
    private val statusBar = StatusBar()

    init {
        JGL.dialogOwner = this
        // Menu bar
        jMenuBar = JMenuBar().apply {
            add(JMenu("File").apply {
                setMnemonic('F')
                add(MenuItem.create("Export...", 'E') { Dialogs.showExport() })
                add(MenuItem.create("Import...", 'I') { Dialogs.showImport { refresh() } })
                add(MenuItem.create("Print", 'P', KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK)) { Dialogs.showPrint(this@MainFrame) })
                add(MenuItem.create("Exit", 'x', KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK)) { exitProcess(0) })
            })
            add(JMenu("Edit").apply {
                setMnemonic('E')
                add(MenuItem.create("New", 'N', KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK)) { new() })
                add(MenuItem.create("Delete", 'D', KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0)) { delete() })
                add(MenuItem.create("Refresh", 'R', KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK)) { refresh() })
                add(MenuItem.create("Show groups", 'g') { Dialogs.showGroups() })
                add(MenuItem.create("Settings", 'S') { settings() })
            })
            add(JMenu("Help").apply {
                setMnemonic('H')
                add(MenuItem.create("Check for Updates") { Updates.check(statusBar) })
                add(MenuItem.create("About", FlatHelpButtonIcon(), 'A') { Dialogs.showAbout() })
            })
        }
        // Components
        add(JPanel(GridLayout(1, 2)).apply {
            // Contacts
            add(JPanel().apply ContactsPanel@{
                this.layout = BorderLayout()
                this.add(JPanel(BorderLayout()).apply {
                    add(JPanel().apply {
                        add(Button.create("New") { new() })
                        add(Button.create("Edit") { edit() })
                        add(Button.create("Delete") { delete() })
                        add(Button.create("Refresh") { refresh() })
                    }, BorderLayout.WEST)
                    add(JTextField(20).apply SearchField@{
                        var previousText = ""
                        this@SearchField.addCaretListener {
                            if (this@SearchField.text != previousText) {
                                if (this@SearchField.text == "") list.setListData(UDM.data.contacts.toTypedArray())
                                else {
                                    val contacts = ArrayList<Contact>()
                                    for (contact in UDM.data.contacts) {
                                        if (contact.firstName.contains(this@SearchField.text, true) || contact.lastName.contains(this@SearchField.text, true)) contacts.add(contact)
                                        list.setListData(contacts.toTypedArray())
                                    }
                                }
                            }
                            previousText = this@SearchField.text
                        }
                        this@SearchField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Search")
                        this@SearchField.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, FlatSearchIcon())
                        this@SearchField.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true)
                    })
                }, BorderLayout.NORTH)
                list.cellRenderer = ContactRenderer()
                list.addMouseListener(object : MouseAdapter() {
                    override fun mouseClicked(e: MouseEvent) {
                        if (e.clickCount == 2) edit()
                    }
                })
                this.add(JScrollPane(list))
            })
            //
            add(JPanel(BorderLayout()).apply DetailPanel@{
                this.isVisible = false
                val nameLabel = JLabel().apply { font = Font(font.name, Font.PLAIN, font.size + 20) }
                val detailLabel = JLabel().apply { verticalAlignment = JLabel.TOP }
                list.addListSelectionListener {
                    if (list.selectedIndex >= 0) {
                        val contact = UDM.data.contacts[list.selectedIndex]
                        nameLabel.text = "${contact.firstName} ${contact.lastName}"
                        detailLabel.text = """
                        <html>
                        First name: ${contact.firstName}
                        <br>
                        Last name: ${contact.lastName}
                        <br>
                        Birthday: ${contact.birthday.toReadableString()}
                        <br>
                        Job: ${contact.job.notNull()}
                        <br>
                        Handy number: ${contact.handyNumber.notNull()}
                        <br>
                        Number: ${contact.number.notNull()}
                        <br>
                        Number work: ${contact.numberWork.notNull()}
                        <br>
                        Email: ${contact.email.notNull()}
                        <br>
                        Group: ${contact.group.notNull()}
                        </html>
                    """.trimIndent()
                        this@DetailPanel.isVisible = true
                    } else this@DetailPanel.isVisible = false
                }
                this.add(JPanel(BorderLayout(10, 10)).apply {
                    add(JLabel(avatarIcon), BorderLayout.WEST)
                    add(nameLabel)
                }, BorderLayout.NORTH)
                this.add(detailLabel)
            })
        })
        add(statusBar, BorderLayout.SOUTH)

        this.pack()
        this.iconImages = logos
        this.defaultCloseOperation = DISPOSE_ON_CLOSE
        this.extendedState = MAXIMIZED_BOTH
        this.setLocationRelativeTo(null)
        this.title = "JContacts"
        this.isVisible = true
        if (!UDM.data.licenseAgreed) Dialogs.showLicense { refresh() }
        Updates.check(statusBar)
    }

    private fun new() {
        val result = contactDialog("New", null)
        if (result != null) {
            UDM.data.contacts.add(result)
            UDM.write()
            refresh()
        }
    }

    private fun edit() {
        val idx = list.selectedIndex
        if (idx > -1) {
            val result = contactDialog("Edit", UDM.data.contacts[idx])
            if (result != null) {
                UDM.data.contacts[idx] = result
                UDM.write()
                refresh()
            }
        } else noSelection()
    }

    private fun contactDialog(dialogTitle: String, default: Contact?): Contact? {
        var returnValue: Contact? = null
        // Dialog
        val firstNameIn = JTextField(20)
        val lastNameIn = JTextField(20)
        if (default != null) {
            firstNameIn.text = default.firstName
            lastNameIn.text = default.lastName
        }
        val birthday = JCheckBox("Birthday: ", true)
        val birthdayIn = BirthdayPanel()
        default?.birthday.ifNull(birthday, birthdayIn) { birthdayIn.birthday = this }
        birthday.addChangeListener { birthdayIn.isEnabled = birthday.isSelected }
        val job = JCheckBox("Job: ")
        val jobIn = JComboBox(Jobs.jobs).apply { isEnabled = false }
        default?.job.ifNull(job, jobIn) { jobIn.selectedItem = this }
        job.addChangeListener { jobIn.isEnabled = job.isSelected }
        val handyNumber = JCheckBox("Handy number: ")
        val handyNumberIn = JTextField(20).apply { isEnabled = false }
        default?.handyNumber.ifNull(handyNumber, handyNumberIn) { handyNumberIn.text = this }
        handyNumber.addChangeListener { handyNumberIn.isEnabled = handyNumber.isSelected }
        val number = JCheckBox("Number: ")
        val numberIn = JTextField(20).apply { isEnabled = false }
        default?.number.ifNull(number, numberIn) { numberIn.text = this }
        number.addChangeListener { numberIn.isEnabled = number.isSelected }
        val numberWork = JCheckBox("Number work: ")
        val numberWorkIn = JTextField(20).apply { isEnabled = false }
        default?.numberWork.ifNull(numberWork, numberWorkIn) { numberWorkIn.text = this }
        numberWork.addChangeListener { numberWorkIn.isEnabled = numberWork.isSelected }
        val email = JCheckBox("Email: ")
        val emailIn = JTextField(20).apply { isEnabled = false }
        default?.email.ifNull(email, emailIn) { emailIn.text = this }
        email.addChangeListener { emailIn.isEnabled = email.isSelected }
        val group = JCheckBox("Group: ")
        val groupIn = JComboBox(arrayOf("").plus(UDM.data.getGroups())).apply { isEnabled = false }
        default?.group.ifNull(group, groupIn) { groupIn.selectedItem = this }
        group.addChangeListener { groupIn.isEnabled = group.isSelected }
        Dialog.showDialog("$dialogTitle Contact", {
            JPanel(GridLayout(9, 1)).apply {
                add(JPanel(BorderLayout()).apply {
                    add(JLabel("First name: "), BorderLayout.WEST)
                    add(firstNameIn)
                })
                add(JPanel(BorderLayout()).apply {
                    add(JLabel("Last name: "), BorderLayout.WEST)
                    add(lastNameIn)
                })
                add(JPanel(BorderLayout()).apply {
                    add(birthday, BorderLayout.WEST)
                    add(birthdayIn)
                })
                add(JPanel(BorderLayout()).apply {
                    add(job, BorderLayout.WEST)
                    jobIn.isEditable = true
                    add(jobIn)
                })
                add(JPanel(BorderLayout()).apply {
                    add(handyNumber, BorderLayout.WEST)
                    add(handyNumberIn)
                })
                add(JPanel(BorderLayout()).apply {
                    add(number, BorderLayout.WEST)
                    add(numberIn)
                })
                add(JPanel(BorderLayout()).apply {
                    add(numberWork, BorderLayout.WEST)
                    add(numberWorkIn)
                })
                add(JPanel(BorderLayout()).apply {
                    add(email, BorderLayout.WEST)
                    add(emailIn)
                })
                add(JPanel(BorderLayout()).apply {
                    add(group, BorderLayout.WEST)
                    groupIn.isEditable = true
                    add(groupIn)
                })
            }
        }, {
            returnValue = Contact(
                firstNameIn.text,
                lastNameIn.text,
                if (birthday.isSelected) birthdayIn.birthday else null,
                if (job.isSelected && !jobIn.selectedItem?.toString().isNullOrBlank()) jobIn.selectedItem?.toString() else null,
                if (handyNumber.isSelected && !handyNumberIn.text.isNullOrBlank()) handyNumberIn.text else null,
                if (number.isSelected && !numberIn.text.isNullOrBlank()) numberIn.text else null,
                if (numberWork.isSelected && !numberWorkIn.text.isNullOrBlank()) numberWorkIn.text else null,
                if (email.isSelected && !emailIn.text.isNullOrBlank()) emailIn.text else null,
                if (group.isSelected && !groupIn.selectedItem?.toString().isNullOrBlank()) groupIn.selectedItem?.toString() else null
            )
        }, {})
        return returnValue
    }

    private fun delete() {
        val idx = list.selectedIndex
        if (idx > -1) {
            if (OptionPane.showConfirmDialog("Are you sure to delete selected contact?", "Delete Contact", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                UDM.data.contacts.removeAt(idx)
                UDM.write()
                refresh()
            }
        } else noSelection()
    }

    private fun settings() {
        // Theme
        val themes = arrayOf(Theme.JCONTACTS_LIGHT, Theme.JCONTACTS_DARK, Theme.LIGHT, Theme.DARK, Theme.INTELLIJ, Theme.DARCULA, Theme.MAC_LIGHT, Theme.MAC_DARK)
        val themeBox = JComboBox(arrayOf("JContacts Light", "JContacts Dark", "Light", "Dark", "IntelliJ", "Darcula", "Mac Light", "Mac Dark"))
        val titleStringField = JTextField(20)
        val detailStringField = JTextField(20)
        val detailStringEnabledCheckBox = JCheckBox("Detail")
        val sortBy = arrayOf(SortBy.NONE, SortBy.FIRST_NAME, SortBy.LAST_NAME, SortBy.GROUP)
        val sortByBox = JComboBox(arrayOf("None", "First name", "Last name", "Group"))
        //
        Dialog.showDialog("Settings", {
            JPanel(GridLayout(5, 1)).apply {
                add(JPanel(BorderLayout()).apply {
                    add(JLabel("Theme: "), BorderLayout.WEST)
                    themeBox.selectedIndex = themes.indexOf(UDM.data.settings.theme)
                    add(themeBox)
                })
                add(JPanel(BorderLayout()).apply {
                    add(JLabel("Title: "), BorderLayout.WEST)
                    titleStringField.text = UDM.data.settings.titleString
                    add(titleStringField)
                })
                add(JPanel(BorderLayout()).apply {
                    add(JLabel("Detail: "), BorderLayout.WEST)
                    detailStringField.text = UDM.data.settings.detailString
                    add(detailStringField)
                })
                detailStringEnabledCheckBox.isSelected = UDM.data.settings.detailStringEnabled
                add(detailStringEnabledCheckBox)
                add(JPanel(BorderLayout()).apply {
                    add(JLabel("Sort by: "), BorderLayout.WEST)
                    sortByBox.selectedIndex = sortBy.indexOf(UDM.data.settings.sortBy)
                    add(sortByBox)
                })
            }
        }, {
            UDM.data.settings =
                UserSettings(themes[themeBox.selectedIndex], titleStringField.text, detailStringField.text, detailStringEnabledCheckBox.isSelected, sortBy[sortByBox.selectedIndex])
            UDM.write()
            refresh()
        }, {})
    }

    private fun noSelection() {
        OptionPane.showInformation("No contact selected!")
    }

    private fun refresh() {
        UDM.refresh()
        list.setListData(UDM.data.contacts.toTypedArray())
    }

    private class BirthdayPanel : JPanel() {
        private val now = LocalDateTime.now()
        private val day = SpinnerNumberModel(now.dayOfMonth, 1, 31, 1)
        private val month = SpinnerNumberModel(now.monthValue, 1, 12, 1)
        private val year = SpinnerNumberModel(now.year, 1900, now.year, 1)
        private val hour = SpinnerNumberModel(now.hour, 1, 24, 1)
        private val minute = SpinnerNumberModel(now.hour, 0, 59, 1)
        private val time = JCheckBox("Time ")
        var birthday: Birthday = Birthday(now.dayOfMonth, now.monthValue, now.year)
            set(value) {
                field = value
                day.value = value.day
                month.value = value.month
                year.value = value.year
                if (value.hour != null && value.minute != null) {
                    hour.value = value.hour
                    minute.value = value.minute
                } else time.isSelected = false
            }
            get() = if (time.isSelected) Birthday(day.number.toInt(), month.number.toInt(), year.number.toInt(), hour.number.toInt(), minute.number.toInt())
            else Birthday(day.number.toInt(), month.number.toInt(), year.number.toInt())

        init {
            add(JSpinner(day))
            add(JLabel("."))
            add(JSpinner(month))
            add(JLabel("."))
            add(JSpinner(year))
            add(JLabel(" "))
            add(time)
            add(JSpinner(hour))
            add(JLabel(":"))
            add(JSpinner(minute))
        }

        override fun setEnabled(enabled: Boolean) {
            super.setEnabled(enabled)
            for (child in components) {
                child.isEnabled = enabled
            }
        }
    }
}
