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

import de.janorico.jcontacts.gui.StatusBar
import de.janorico.jgl.helpers.OptionPane
import java.io.*
import java.net.*
import java.util.*
import javax.swing.JOptionPane
import kotlin.math.roundToInt
import kotlin.system.exitProcess

object Updates {
    fun check(statusBar: StatusBar) {
        try {
            statusBar.indeterminateProgress("Receiving update info...")
            val stream = URI("https://github.com/Janorico/Versions/raw/main/JContacts.properties").toURL().openStream()
            val properties = Properties()
            properties.load(stream)
            stream.close()
            statusBar.stopProgress()
            val newVersionKey = (properties.getProperty("newest-version.key", null) ?: throw IOException("Can't get newest version key!")).toFloatOrNull()
                ?: throw IOException("Can't convert newest version key to float!")
            if (newVersionKey > 2.0) {
                val newVersionName = properties.getProperty("newest-version.display-name", null) ?: throw IOException("Can't get newest version name!")
                if (OptionPane.showOptionDialog("$newVersionName is available!", "Update available", arrayOf("Update", "Do nothing"), 0) == 0) {
                    if (System.getProperty("os.name").contains("windows", true)) {
                        val newVersionWindowsInstaller =
                            properties.getProperty("newest-version.windows-installer", null) ?: throw IOException("Can't get newest version windows installer URL!")
                        val outFile = File(System.getProperty("user.home") + "/Downloads", "$newVersionName-Installer.exe")
                        if (downloadFile(URI(newVersionWindowsInstaller).toURL(), outFile, statusBar)) {
                            if (OptionPane.showConfirmDialog("Updates downloaded! Install now (JContacts will be exited)?", "Update downloaded", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                                Runtime.getRuntime().exec(arrayOf(outFile.path))
                                exitProcess(0)
                            } else OptionPane.showInformation("To install exit JContacts and run this file: ${outFile.path}.")
                        } else throw IOException("Error while downloading!")
                    } else throw UnsupportedOperationException("Auto-update works only on Windows!")
                }
            } else statusBar.displayMessage("You are running the newest version of JContacts.")
        } catch (e: Exception) {
            statusBar.stopProgress()
            statusBar.displayError("Can't check for updates (${e::class.qualifiedName})! Message: ${e.message}")
        }
    }

    private fun downloadFile(from: URL, to: File, statusBar: StatusBar): Boolean {
        val connection = from.openConnection()
        val totalSize = connection.contentLength
        val progress = statusBar.progress((totalSize / 1_000_000f).roundToInt(), "MB")
        val bis = BufferedInputStream(connection.getInputStream())
        if (!to.isFile) to.createNewFile()
        val fos = FileOutputStream(to)
        val bos = BufferedOutputStream(fos, 1024)
        val data = ByteArray(1024)
        var downloadedSize = 0
        var downloadStep: Int
        while (bis.read(data, 0, 1024).also { downloadStep = it } >= 0) {
            downloadedSize += downloadStep
            progress((downloadedSize / 1_000_000f).roundToInt())
            bos.write(data, 0, downloadStep)
        }
        statusBar.stopProgress()
        // Close Streams
        bos.close()
        fos.close()
        bis.close()
        return to.isFile
    }
}
