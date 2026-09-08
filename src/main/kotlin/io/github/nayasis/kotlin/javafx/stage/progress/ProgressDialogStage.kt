package io.github.nayasis.kotlin.javafx.stage.progress

import io.github.nayasis.kotlin.javafx.control.basic.setMoveHandler
import io.github.nayasis.kotlin.javafx.control.basic.vmargin
import io.github.nayasis.kotlin.javafx.geometry.Insets
import io.github.nayasis.kotlin.javafx.stage.loadDefaultIcon
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.ProgressBar
import io.github.nayasis.kotlin.javafx.misc.runLater
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.Region.USE_PREF_SIZE
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.stage.StageStyle

@Suppress("MemberVisibilityCanBePrivate")
class ProgressDialogStage: Stage {

    val titleHeader  = Label().apply { styleClass.add("title") }
    val progressBars = ArrayList<ProgressBar>()
    val messages     = ArrayList<Label>()
    val subMessages  = ArrayList<Label>()
    val headerRight  = HBox().apply {
        alignment = Pos.CENTER_RIGHT
        padding   = Insets(0.0, 4.0, 0.0, 0.0)
    }

    constructor(progressCount: Int) {

        for(i in 0 until progressCount) {
            messages.add(Label().apply { styleClass.add("message") })
            subMessages.add(Label().apply {
                styleClass.add("sub-message")
                minWidth = USE_PREF_SIZE
            })
            progressBars.add(ProgressBar().apply {
                maxWidth = Double.MAX_VALUE
                progress = 0.0
                // margin is not supported by CSS
                vmargin = Insets(0,0,7,0)
            })
        }

        val root = VBox().apply {
            children.add(HBox().apply {
                styleClass.add("header")
                alignment = Pos.CENTER_LEFT
                children.add(titleHeader)
                children.add(Region().apply { HBox.setHgrow(this, Priority.ALWAYS) })
                children.add(headerRight)
                // margin is not supported by CSS
                vmargin = Insets(0,0,7,0)
            })
            for( i in 0 until progressCount) {
                children.add(HBox().apply {
                    styleClass.add("description")
                    children.add(messages[i])
                    children.add(Region().apply { HBox.setHgrow(this, Priority.ALWAYS) })
                    children.add(subMessages[i])
                })
                children.add(progressBars[i])
            }
            stylesheets.run {
                add("basicafx/css/progress-bar.css")
                add("basicafx/css/dialog-progress.css")
            }
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

    fun updateProgress(index: Int, percent: Number) {
        progressBars[index].progress = percent.toDouble()
    }

    @Suppress("LocalVariableName")
    fun updateProgress(index: Int, done: Number, max: Number) {
        val _done = done.toDouble()
        val _max  = max.toDouble()
        progressBars[index].progress = when {
            _done.isInfinite() || _done.isNaN() -> 0.0
            _max.isInfinite()  || _max.isNaN() -> 0.0
            else -> _done / _max
        }
    }

    fun updateMessage(index: Int, message: String?) {
        runLater {
            messages[index].text = message ?: ""
        }
    }

    fun updateSubMessage(index: Int, message: String?) {
        runLater {
            subMessages[index].text = message ?: ""
        }
    }

    fun updateSubMessageAsProgress(index: Int, format: String = "%.1f%%") {
        updateSubMessage(index,format.format(progressBars[index].progress*100))
    }

    fun updateTitle(title: String?) {
        runLater { titleHeader.text = title ?: "" }
    }

    fun addHeaderRight(node: Node) {
        headerRight.children.add(node)
    }

}
