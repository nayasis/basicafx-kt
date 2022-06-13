package com.github.nayasis.kotlin.javafx.stage.progress

import com.github.nayasis.kotlin.javafx.control.basic.setMoveHandler
import com.github.nayasis.kotlin.javafx.control.basic.vmargin
import com.github.nayasis.kotlin.javafx.geometry.Insets
import com.github.nayasis.kotlin.javafx.stage.loadDefaultIcon
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.ProgressBar
import javafx.scene.layout.Priority
import javafx.scene.layout.Region.USE_PREF_SIZE
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.stage.StageStyle
import tornadofx.attachTo
import tornadofx.hbox
import tornadofx.hgrow
import tornadofx.region
import tornadofx.runLater
import tornadofx.vbox

@Suppress("MemberVisibilityCanBePrivate")
class ProgressDialogStage: Stage {

    val titleHeader  = Label().apply { styleClass.add("title") }
    val progressBars = ArrayList<ProgressBar>()
    val messages     = ArrayList<Label>()
    val subMessages  = ArrayList<Label>()

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

        val root = vbox {
            hbox {
                styleClass.add("header")
                titleHeader.attachTo(this)
                // margin is not supported by CSS
                vmargin = Insets(0,0,7,0)
            }
            for( i in 0 until progressCount) {
                hbox {
                    styleClass.add("description")
                    messages[i].attachTo(this)
                    region { hgrow = Priority.ALWAYS }
                    subMessages[i].attachTo(this)
                }
                progressBars[i].attachTo(this)
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

    fun updateTitle(title: String?) {
        runLater { titleHeader.text = title ?: "" }
    }

}