package io.github.nayasis.kotlin.javafx.property

import javafx.stage.Stage
import java.io.Serializable

data class PositionProperty(
    var x: Int = 0,
    var y: Int = 0,
): Serializable {

    constructor(stage: Stage?): this() { stage?.let { read(it) } }

    fun read(stage: Stage?) {
        stage?.let {
            x = it.x.toInt()
            y = it.y.toInt()
        }
    }

    fun bind(stage: Stage?) {
        stage?.let {
            it.x = x.toDouble()
            it.y = y.toDouble()
        }
    }

}