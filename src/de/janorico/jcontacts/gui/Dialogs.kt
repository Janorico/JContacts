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
import de.janorico.jcontacts.gui.Dialogs.FontUtil.drawCenteredString
import de.janorico.jgl.JGL
import de.janorico.jgl.components.FontChooser
import de.janorico.jgl.helpers.*
import de.janorico.jgl.helpers.Button
import de.janorico.jgl.helpers.Dialog
import java.awt.*
import java.awt.event.*
import java.awt.font.*
import java.awt.geom.Rectangle2D
import java.io.File
import java.time.LocalDate
import javax.swing.*
import javax.swing.event.HyperlinkEvent
import javax.swing.filechooser.FileNameExtensionFilter
import kotlin.math.roundToInt
import kotlin.system.exitProcess

object Dialogs {
    private val xmlFilter = FileNameExtensionFilter("XML Files (*.xml)", "xml")
    val copyright = """
        JContacts is a simple contact software, written in Kotlin.
        Copyright (C) 2023 Janosch Lion

        This program is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License
        along with this program.  If not, see <https://www.gnu.org/licenses/>.
        """.trimIndent()
    private val about = """
        <html>
        <h1 align="center">JContacts</h1>
        <h2 align="center">A Janorico Product</h2>
        <p>
        Author: Janosch Lion
        <br>
        Email: janorico@posteo.de
        <br>
        Website: https://github.com/Janorico/JContacts
        <br>
        License: GNU General Public License v3.0 (See 'Copyright' and 'Licenses')
        <br>
        THIS PROGRAM INCLUDES FlatLaf (see https://github.com/JFormDesigner/FlatLaf),
        <br>
        JGL 2.0 (see https://github.com/Janorico/JGL) AND
        <br>
        WAS COMPILED WITH THE '-include-runtime' PARAMETER.
        </p>
        </html>
    """.trimIndent()

    fun showAbout() {
        Dialog.showDialog("About", {
            it.minimumSize = Dimension(450, 600)
            var wSize = Dimension(850, 1000)
            JGL.dialogOwner.notNull { wSize = this.size }
            it.preferredSize = Dimension(wSize.width - 400, wSize.height - 400)
            JTabbedPane().apply {
                addTab("Copyright", JLabel("<html><pre>$copyright</pre></html>", JLabel.CENTER))
                addTab("About", JLabel(about, JLabel.CENTER))
                addTab("Licenses", JSplitPane().apply {
                    val gpl = RM.getURL("gpl.html")
                    val apache2 = RM.getURL("apache-2.0.txt")
                    val pane = JEditorPane(gpl).apply {
                        isEditable = false
                        addHyperlinkListener { e: HyperlinkEvent -> if (e.eventType == HyperlinkEvent.EventType.ACTIVATED) Desktop.getDesktop().browse(e.url.toURI()) }
                    }
                    rightComponent = JScrollPane(pane)
                    leftComponent = JList(arrayOf("GNU General Public License v3.0 (JContacts and JGL)", "Apache 2.0 (FlatLaf)")).apply {
                        selectedIndex = 0
                        addListSelectionListener {
                            when (selectedIndex) {
                                0 -> pane.page = gpl
                                1 -> pane.page = apache2
                            }
                        }
                    }
                })
            }
        }, { dialog: JDialog -> Button.create("Close") { dialog.dispose() } })
    }

    fun showLicense(refresh: () -> Unit) {
        Dialog.showDialog("License Agreement", { JLabel("<html><pre>$copyright</pre></html>") }, { dialog: JDialog ->
            dialog.defaultCloseOperation = JDialog.DO_NOTHING_ON_CLOSE
            dialog.addWindowListener(object : WindowAdapter() {
                override fun windowClosing(e: WindowEvent?) {
                    exitProcess(0)
                }
            })
            return@showDialog JPanel(FlowLayout(FlowLayout.RIGHT)).apply {
                add(Button.create("I Agree") {
                    UDM.data.licenseAgreed = true
                    UDM.write()
                    refresh()
                    dialog.dispose()
                })
                add(Button.create("Exit") { exitProcess(0) })
            }
        })
    }

    fun showImport(refresh: () -> Unit) {
        FileChooser.openFile({ selectedFile: File?, result: Int ->
            if (result == JFileChooser.APPROVE_OPTION && selectedFile != null) {
                val importedData = UDM.load(selectedFile)
                // GUI
                val replaceSettings = JCheckBox("Replace Settings")
                val selectList = JList(importedData.contacts.toTypedArray())
                Dialog.showDialog("Import", {
                    it.preferredSize = Dimension(800, 600)
                    JPanel(BorderLayout()).apply {
                        add(JPanel(GridLayout(2, 1)).apply OptionsPanel@{
                            add(replaceSettings)
                            add(JLabel("Select contacts you want to import:"))
                        }, BorderLayout.NORTH)
                        selectList.selectionMode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
                        selectList.cellRenderer = ContactRenderer()
                        selectList.selectedIndices = IntArray(importedData.contacts.size) { idx -> idx }
                        add(JScrollPane(selectList))
                    }
                }, {
                    if (replaceSettings.isSelected) {
                        UDM.data.settings = importedData.settings
                    }
                    for (contact in selectList.selectedValuesList) UDM.data.contacts.add(contact)
                    UDM.write()
                    refresh()
                }, {})
            }
        }, xmlFilter)
    }

    fun showExport() {
        FileChooser.saveFile({ selectedFile: File?, result: Int ->
            if (result == JFileChooser.APPROVE_OPTION && selectedFile != null) {
                var file: File = selectedFile
                if (!file.path.endsWith(".xml")) file = File("${selectedFile.path}.xml")
                UDM.write(file)
            }
        }, xmlFilter)
    }

    fun showPrint(frame: Frame) {
        val columns = ArrayList<Column>()
        val fontChooser = FontChooser(10, 20, Font("Arial", Font.PLAIN, 12))
        val columnStrings = arrayOf("<none>").plus(Column.values().map { it.displayName })
        val columnComboBoxes = Array(Column.values().size - 2) {
            JComboBox(columnStrings)
        }
        val contactsList = JList(UDM.data.contacts.toTypedArray())
        Dialog.showDialog("Print - Choose Columns", {
            JPanel(BorderLayout()).apply {
                add(JPanel(GridLayout(5, 1)).apply {
                    add(JLabel("Font:"))
                    add(fontChooser)
                    add(JLabel("Columns:"))
                    add(JPanel(GridLayout(1, columnComboBoxes.size)).apply { for (comboBox in columnComboBoxes) add(comboBox) })
                    add(JLabel("Select contacts you want to print:"))
                }, BorderLayout.NORTH)
                contactsList.selectionMode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
                contactsList.cellRenderer = ContactRenderer()
                contactsList.selectedIndices = IntArray(UDM.data.contacts.size) { idx -> idx }
                add(JScrollPane(contactsList))
            }
        }, {
            for (comboBox in columnComboBoxes) {
                val selection = comboBox.selectedIndex - 1
                if (selection > -1) columns.add(Column.values()[selection])
            }
            val dataToPrint = ArrayList<Contact>()
            for (contact in contactsList.selectedValuesList) dataToPrint.add(contact)
            if (columns.size > 0) print(columns.toTypedArray(), dataToPrint.toTypedArray(), fontChooser.getActiveFont(Font.PLAIN), frame)
            else OptionPane.showInformation("No columns selected!")
        }, {})
    }

    private fun print(columns: Array<Column>, data: Array<Contact>, font: Font, frame: Frame) {
        Toolkit.getDefaultToolkit().getPrintJob(frame, "JContacts: Print Contacts", null).notNull {
            printPage(columns, data, font, this)
            this.end()
        }
    }

    private fun printPage(columns: Array<Column>, data: Array<Contact>, font: Font, printJob: PrintJob) {
        val size = printJob.pageDimension
        val g = printJob.graphics
        // Set font
        g.font = font
        // Header
        val now = LocalDate.now()
        var startY = g.drawCenteredString("Printed with JContacts at ${now.dayOfWeek}, ${now.monthValue}/${now.dayOfMonth}/${now.year}", 10, size.width) + 10
        val endY = size.height - 10
        // region Border, Lines and Headers
        // Border
        g.drawRect(10, startY, size.width - 20, endY - startY)
        // Columns
        val difference = (size.width - 20) / columns.size
        // Column lines
        for (i in 1 until columns.size) {
            val x = (10 + (difference * i))
            g.drawLine(x, startY, x, endY)
        }
        // Column headers
        var tempY = startY
        for (i in columns.indices) {
            val x = (10 + (difference * (i + 1)))
            tempY = g.drawCenteredString(columns[i].displayName, (startY + 2), x, (x - difference)) + 4
        }
        startY = tempY
        g.drawLine(10, startY, size.width - 10, startY)
        // endregion
        // Data
        for (i in data.indices) {
            for (j in columns.indices) {
                val x = (10 + (difference * (j + 1)))
                columns[j].string(data[i]).notNull {
                    (g.drawCenteredString(this, (startY + 2), x, (x - difference)) + 4).apply {
                        if (this > tempY) tempY = this
                    }
                }
            }
            startY = tempY
            g.drawLine(10, startY, size.width - 10, startY)
            if (startY >= (endY - font.size)) {
                g.dispose()
                printPage(columns, data.copyOfRange((i + 1), data.size), font, printJob)
                return
            }
        }
    }

    private enum class Column(val displayName: String) {
        NAME("Name"), FIRST_NAME("First name"), LAST_NAME("Last name"), BIRTHDAY("Birthday"), JOB("Job"),
        HANDY_NUMBER("Handy number"), NUMBER("Number"), NUMBER_WORK("Number work"), EMAIL("Email"), GROUP("Group");

        fun string(contact: Contact): String? = when (this) {
            NAME -> "${contact.firstName} ${contact.lastName}"
            FIRST_NAME -> contact.firstName
            LAST_NAME -> contact.lastName
            BIRTHDAY -> contact.birthday.toReadableNullableString()
            JOB -> contact.job
            HANDY_NUMBER -> contact.handyNumber
            NUMBER -> contact.number
            NUMBER_WORK -> contact.numberWork
            EMAIL -> contact.email
            GROUP -> contact.group
        }
    }

    private object FontUtil {
        private fun getFontBounds(font: Font, str: String): Rectangle2D {
            val renderContext = FontRenderContext(font.transform, false, false)
            val textLayout = TextLayout(str, font, renderContext)
            return textLayout.bounds
        }

        fun Graphics.drawCenteredString(str: String, y: Int, end: Int, start: Int = 0): Int {
            if (start >= end) throw IllegalArgumentException("Start is greater or equal to end!")
            val size = getFontBounds(this.font, str)
            val width = end - start
            drawString(str, (start + ((width - size.width) / 2)).roundToInt(), y + size.height.roundToInt())
            return size.height.roundToInt() + y
        }
    }
}
