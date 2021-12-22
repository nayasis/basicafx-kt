package com.github.nayasis.kotlin.javafx.property

import javafx.stage.Stage
import java.io.Serializable

data class SizeProperty(
    var width: Int = 400,
    var height: Int = 300,
): Serializable {

    constructor(stage: Stage?): this() { stage?.let { read(it) } }

    fun read(stage: Stage?) {
        stage?.let {
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

}