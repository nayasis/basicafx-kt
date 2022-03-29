@file:Suppress("MemberVisibilityCanBePrivate")

package com.github.nayasis.kotlin.javafx.stage

import com.github.nayasis.kotlin.javafx.model.Point
import javafx.concurrent.Task
import javafx.concurrent.Worker.State.*
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.ButtonType.CANCEL
import javafx.scene.control.Dialog
import javafx.scene.control.Label
import javafx.scene.control.ProgressBar
import javafx.scene.paint.Color
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.stage.Window
import mu.KotlinLogging
import tornadofx.label
import tornadofx.progressbar
import tornadofx.runAsync
import tornadofx.runLater
import tornadofx.vbox

private val logger = KotlinLogging.logger {}

//@Suppress("HasPlatformType")
class ProgressDialog(task: Task<*>?) {

    val dialog = ProgressDialogCore(task)
    val scene: Scene
        get() = dialog.dialogPane.scene
    val stage: Stage
        get() = dialog.dialogPane.scene.window as Stage

    var title: String?
        get() = dialog.dialogPane.headerText ?: ""
        set(value) {
            dialog.updateTitle(value ?: "")
        }
    var progress: Double
        get() = dialog.progressBar.progress
        set(value) {
            dialog.progressBar.progress = value
        }
    var message: String?
        get() = dialog.message.text ?: ""
        set(value) {
            dialog.message.text = value ?: ""
        }

    init {
        title = ""
        message = ""
        stage.addCloseRequest {
            dialog.closeForcibly()
        }
    }

    fun updateMessage(message: String?) = dialog.updateMessage(message)
    fun updateProgress(workDone: Int, max: Int) = dialog.updateProgress(workDone,max)
    fun updateProgress(workDone: Long, max: Long) = dialog.updateProgress(workDone,max)
    fun updateProgress(workDone: Double, max: Double) = dialog.updateProgress(workDone,max)

    fun initModality(modality: Modality) = dialog.initModality(modality)
    fun initOwner(window: Window?) = dialog.initOwner(window)

    fun close() = dialog.closeForcibly()

    fun runSync() {
        dialog.task?.let { task ->
            runAsync { task.run() }
        }
        dialog.showAndWait()
    }

    fun runAsync() {
        dialog.show()
        dialog.task?.let { task ->
            runAsync { task.run() }
        }
    }

}

class ProgressDialogCore: Dialog<Any> {

    lateinit var message: Label
    lateinit var progressBar: ProgressBar

    var task: Task<*>? = null

    constructor(task: Task<*>?) {

        this.task = task

        dialogPane.stylesheets.add("basicafx/css/dialog-progress.css")

        dialogPane.scene.apply {
            fill = Color.TRANSPARENT
            (window as Stage).apply {
                initStyle(StageStyle.TRANSPARENT)
                isAlwaysOnTop = true
                loadDefaultIcon()
                setMoveHandler(dialogPane)
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

        if( task != null ) {
            if( task.state in setOf(CANCELLED, FAILED, SUCCEEDED) )
                throw IllegalArgumentException("worker state is not valid (${task.state})")
            progressBar.progressProperty().bind(task.progressProperty())
            task.titleProperty().addListener { _, _, text -> dialogPane.headerText = text ?: "" }
            task.messageProperty().addListener { _, _, text -> message.text = text ?: "" }
            task.stateProperty().addListener { _, _, state ->
                when (state) {
                    CANCELLED, FAILED, SUCCEEDED -> closeForcibly()
                    SCHEDULED -> runLater { show() }
                    else -> {}
                }
            }
        }

    }

    fun updateProgress(workDone: Int, max: Int) = updateProgress(workDone.toDouble(),max.toDouble())

    fun updateProgress(workDone: Long, max: Long) = updateProgress(workDone.toDouble(),max.toDouble())

    fun updateProgress(workDone: Double, max: Double) = runLater {
        progressBar.progress = when {
            workDone.isInfinite() || workDone.isNaN() -> 0.0
            max.isInfinite() || max.isNaN() -> 0.0
            else -> workDone / max
        }
    }

    fun updateTitle(text: String?) = text?.let { runLater { dialogPane.headerText = it }}

    fun updateMessage(message: String?) = message?.let { runLater { this.message.text = it }}

    fun closeForcibly() {
        runLater {
            progressBar.progressProperty().unbind()
            dialogPane.buttonTypes.add(CANCEL)
            hide()
            dialogPane.buttonTypes.remove(CANCEL)
            close()
        }
    }

    private fun setMoveHandler(node: Node) {
        val offset = Point()
        node.onMousePressed = EventHandler { e ->
            offset.x = e.sceneX
            offset.y = e.sceneY
        }
        node.onMouseDragged = EventHandler { e ->
            x = e.screenX - offset.x
            y = e.screenY - offset.y
            if( "dragged" !in node.styleClass )
                node.styleClass.add("dragged")
        }
        node.onMouseReleased = EventHandler { _ ->
            node.styleClass.remove( "dragged" )
        }
    }

}