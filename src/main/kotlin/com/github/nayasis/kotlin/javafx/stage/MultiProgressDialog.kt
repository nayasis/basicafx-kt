@file:Suppress("MemberVisibilityCanBePrivate")

package com.github.nayasis.kotlin.javafx.stage

import com.github.nayasis.kotlin.javafx.control.basic.setMoveHandler
import javafx.scene.Scene
import javafx.scene.control.Dialog
import javafx.scene.control.Label
import javafx.scene.control.ProgressBar
import javafx.scene.paint.Color
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.stage.Window
import mu.KotlinLogging
import tornadofx.TaskStatus
import tornadofx.View
import tornadofx.attachTo
import tornadofx.awaitUntil
import tornadofx.label
import tornadofx.progressbar
import tornadofx.runAsync
import tornadofx.runLater
import tornadofx.vbox

private val logger = KotlinLogging.logger {}

class MultiProgressDialog(progressCount: Int, task: (dialog: MultiProgressDialog) -> Unit) {

    private val dialog = MultiProgressDialogCore(progressCount)

    val scene: Scene
        get() = dialog.scene
    val stage: Stage
        get() = dialog

    var title: String?
        get() = dialog.title
        set(value) {
            dialog.updateTitle(value ?: "")
        }

    var task = task

    fun initModality(modality: Modality) = dialog.initModality(modality)
    fun initOwner(window: Window?) = dialog.initOwner(window)

    fun updateProgress(index: Int, done: Number, max: Number) {
        dialog.updateProgress(index,done,max)
    }

    fun updateMessage(index: Int, message: String?) {
        dialog.updateMessage(index,message)
    }

    fun updateTitle(title: String?) = dialog.updateTitle(title)

    fun close() {
        runLater {
            dialog.close()
        }
    }

    fun runSync() {
        val self = this
        dialog.show()
        val status = TaskStatus()
        runAsync(status) {
            task.invoke(self)
            self.close()
        }
        status.completed.awaitUntil()
    }

    fun runAsync() {
        val self = this
        dialog.show()
        runAsync {
            task.invoke(self)
            self.close()
        }
    }

}