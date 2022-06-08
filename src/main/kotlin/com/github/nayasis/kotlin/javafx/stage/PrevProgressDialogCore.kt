package com.github.nayasis.kotlin.javafx.stage

import com.github.nayasis.kotlin.javafx.control.basic.setMoveHandler
import javafx.scene.control.ButtonType
import javafx.scene.control.Dialog
import javafx.scene.control.Label
import javafx.scene.control.ProgressBar
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.stage.StageStyle
import tornadofx.label
import tornadofx.progressbar
import tornadofx.runLater
import tornadofx.vbox

class PrevProgressDialogCore: Dialog<Any> {

    lateinit var message: Label
    lateinit var progressBar: ProgressBar

    constructor() {

        dialogPane.stylesheets.add("basicafx/css/dialog-progress.css")

        dialogPane.scene.apply {
            fill = Color.TRANSPARENT
            (window as Stage).apply {
                initStyle(StageStyle.TRANSPARENT)
                isAlwaysOnTop = true
                loadDefaultIcon()
            }
        }

        // set view
        dialogPane.content = vbox {
            message     = label {}
            progressBar = progressbar {
                maxWidth  = Double.MAX_VALUE
                progress  = 0.0
            }
        }

        dialogPane.setMoveHandler("dragged")

    }

    fun updateProgress(workDone: Double, max: Double) = runLater {
        progressBar.progress = when {
            workDone.isInfinite() || workDone.isNaN() -> 0.0
            max.isInfinite() || max.isNaN() -> 0.0
            else -> workDone / max
        }
    }

    fun updateTitle(text: String?) = text?.let { runLater { dialogPane.headerText = it } }

    fun updateMessage(message: String?) = message?.let { runLater { this.message.text = it } }

    fun closeForcibly() {
        runLater {
            progressBar.progressProperty().unbind()
            dialogPane.buttonTypes.add(ButtonType.CANCEL)
            hide()
            dialogPane.buttonTypes.remove(ButtonType.CANCEL)
            close()
        }
    }

}