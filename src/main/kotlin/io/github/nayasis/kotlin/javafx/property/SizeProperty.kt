package io.github.nayasis.kotlin.javafx.property

import javafx.scene.layout.Pane
import javafx.stage.Stage
import java.io.Serializable

data class SizeProperty(
    var width: Int = 400,
    var height: Int = 300,
): Serializable {

    constructor(stage: Stage?): this() { stage?.let { read(it) } }
    constructor(pane: Pane?): this() { pane?.let { read(it) } }

    fun read(stage: Stage?) {
        stage?.let {
            width  = it.width.toInt()
            height = it.height.toInt()
        }
    }

    fun read(pane: Pane?) {
        pane?.let {
            width  = it.width.toInt()
            height = it.height.toInt()
        }
    }

    fun bind(stage: Stage?) {
        stage?.let {
            it.width  = width.toDouble()
            it.height = height.toDouble()
        }
    }

    fun bind(pane: Pane?) {
        pane?.let {
            it.prefWidth  = width.toDouble()
            it.prefHeight = height.toDouble()
        }
    }

}