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

class MultiProgressDialogCore: Stage {

    private val labelTitle = Label()
    private val progressBar = ArrayList<ProgressBar>()
    private val progressMessage = ArrayList<Label>()

    constructor(progressCount: Int) {

        for(i in 0 until progressCount) {
            progressMessage.add(Label())
            progressBar.add(ProgressBar().apply {
                maxWidth = Double.MAX_VALUE
                progress = 0.0
            })
        }

        val root = vbox {
            labelTitle.attachTo(this)
            for( i in 0 until progressCount) {
                progressMessage[i].attachTo(this)
                progressBar[i].attachTo(this)
            }
            stylesheets.add("basicafx/css/dialog-progress.css")
            prefWidth = 300.0
        }

        Scene(root).apply {
            fill = Color.TRANSPARENT
            scene = this
        }

        initStyle(StageStyle.TRANSPARENT)
        isAlwaysOnTop = true
        loadDefaultIcon()
        root.setMoveHandler("dragged")

    }

    fun updateProgress(index: Int, done: Number, max: Number) {
        progressBar[index].let {
            runLater {
                val rdone = done.toDouble()
                val rmax  = max.toDouble()
                it.progress = when {
                    rdone.isInfinite() || rdone.isNaN() -> 0.0
                    rmax.isInfinite() || rmax.isNaN() -> 0.0
                    else -> rdone / rmax
                }
                logger.debug { "- progress : ${it.progress}" }
            }
        }
    }

    fun updateMessage(index: Int, message: String?) {
        runLater {
            progressMessage[index].text = message ?: ""
        }
    }

    fun updateTitle(title: String?) = runLater { labelTitle.text = title ?: "" }

}