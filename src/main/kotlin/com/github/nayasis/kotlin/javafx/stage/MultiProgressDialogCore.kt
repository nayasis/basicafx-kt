package com.github.nayasis.kotlin.javafx.stage

import com.github.nayasis.kotlin.javafx.control.basic.setMoveHandler
import com.github.nayasis.kotlin.javafx.control.basic.vmargin
import com.github.nayasis.kotlin.javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.ProgressBar
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.stage.StageStyle
import tornadofx.attachTo
import tornadofx.hbox
import tornadofx.runLater
import tornadofx.vbox

class MultiProgressDialogCore: Stage {

    private val labelTitle = Label().apply { styleClass.add("title") }
    private val progressBar = ArrayList<ProgressBar>()
    private val progressMessage = ArrayList<Label>()

    constructor(progressCount: Int) {

        for(i in 0 until progressCount) {
            progressMessage.add(Label().apply { styleClass.add("message") })
            progressBar.add(ProgressBar().apply {
                maxWidth = Double.MAX_VALUE
                progress = 0.0
                // margin is not supported by CSS
                vmargin = Insets(0,0,7,0)
            })
        }

        val root = vbox {
            hbox {
                styleClass.add("header")
                labelTitle.attachTo(this)
                // margin is not supported by CSS
                vmargin = Insets(0,0,7,0)
            }
            for( i in 0 until progressCount) {
                progressMessage[i].attachTo(this)
                progressBar[i].attachTo(this)
            }
            stylesheets.add("basicafx/css/dialog-multi-progress.css")
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
            }
        }
    }

    fun updateMessage(index: Int, message: String?) {
        runLater {
            progressMessage[index].text = message ?: ""
        }
    }

    fun updateTitle(title: String?) {
        runLater { labelTitle.text = title ?: "" }
    }

}