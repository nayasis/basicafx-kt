package io.github.nayasis.kotlin.javafx.property

import javafx.stage.Window
import java.io.Serializable

data class PositionProperty(
    var x: Int = 0,
    var y: Int = 0,
): Serializable {

    constructor(window: Window?): this() { window?.let { read(it) } }

    fun read(window: Window?) {
        window?.let {
            x = it.x.toInt()
            y = it.y.toInt()
        }
    }

    fun bind(stage: Window?) {
        stage?.let {
            it.x = x.toDouble()
            it.y = y.toDouble()
        }
    }

}