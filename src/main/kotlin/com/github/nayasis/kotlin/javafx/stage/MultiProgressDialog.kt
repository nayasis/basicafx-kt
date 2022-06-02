@file:Suppress("MemberVisibilityCanBePrivate")

package com.github.nayasis.kotlin.javafx.stage

import com.github.nayasis.kotlin.javafx.model.Point
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
import tornadofx.runAsync
import tornadofx.runLater
import tornadofx.vbox

private val logger = KotlinLogging.logger {}

class MultiProgressDialog(progressCount: Int, task: (dialog: MultiProgressDialog) -> Unit) {

    private val dialog = MultiProgressDialogCore(progressCount)

    val scene: Scene
        get() = dialog.dialogPane.scene
    val stage: Stage
        get() = dialog.dialogPane.scene.window as Stage

    init {
        stage.addCloseRequest {
            dialog.closeForcibly()
        }
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

    fun close() = dialog.closeForcibly()

    fun runSync() {
        val self = this
        runAsync { task.invoke(self) }
        dialog.showAndWait()
    }

    fun runAsync() {
        val self = this
        dialog.show()
        runAsync { task.invoke(self) }
    }

}

class MultiProgressDialogCore: Dialog<Any> {

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

        dialogPane.content = vbox {
            for( i in 0 until progressCount) {
                progressMessage[i]
                progressBar[i]
            }
        }

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
            }
        }
    }

    fun updateMessage(index: Int, message: String?) {
        progressMessage[index].text = message ?: ""
    }

    fun updateTitle(title: String?) = runLater { dialogPane.headerText = title ?: "" }

    fun closeForcibly() {
        runLater {
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