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

import java.awt.*
import javax.swing.*

class StatusBar : JPanel(BorderLayout()) {
    private val label = JLabel()
    private val labelFGColor = label.foreground
    private val messageTimer = Timer(5000) { label.text = "" }
    private val progressBar = JProgressBar()

    init {
        minimumSize = Dimension(1, 20)
        preferredSize = Dimension(width, 20)
        add(label)
        add(JPanel(BorderLayout()).apply {
            progressBar.isVisible = false
            progressBar.minimum = 0
            progressBar.isStringPainted = true
            add(progressBar)
        }, BorderLayout.EAST)
    }

    fun displayMessage(message: String) {
        label.foreground = labelFGColor
        label.text = message
        messageTimer.restart()
    }

    fun displayError(message: String) {
        label.foreground = Color.RED
        label.text = message
        Toolkit.getDefaultToolkit().beep()
        messageTimer.restart()
    }

    fun indeterminateProgress(name: String) {
        stopProgress()
        progressBar.isIndeterminate = true
        progressBar.string = name
        progressBar.isVisible = true
    }

    fun progress(max: Int, suffix: String): (progress: Int) -> Unit {
        stopProgress()
        progressBar.maximum = max
        progressBar.isVisible = true
        return {
            progressBar.value = it
            progressBar.string = "$it/$max $suffix"
        }
    }

    fun stopProgress() {
        progressBar.isVisible = false
        progressBar.string = ""
        progressBar.isIndeterminate = false
    }
}
