package com.github.nayasis.kotlin.javafx.property

import com.github.nayasis.kotlin.javafx.stage.BoundaryChecker
import javafx.scene.control.Dialog
import javafx.stage.Window
import java.io.Serializable

data class InsetProperty(
    var x:      Int = 100,
    var y:      Int = 100,
    var width:  Int = 500,
    var height: Int = 600,
): Serializable {
    constructor(window: Window): this(
        x = window.x.toInt(),
        y = window.y.toInt(),
        width = window.width.toInt(),
        height = window.height.toInt(),
    )
    constructor(dialog: Dialog<*>): this(
        x = dialog.x.toInt(),
        y = dialog.y.toInt(),
        width = dialog.width.toInt(),
        height = dialog.height.toInt(),
    )
    fun read(window: Window) {
        x = window.x.toInt()
        y = window.y.toInt()
        width = window.width.toInt()
        height = window.height.toInt()
    }
    fun bind(window: Window?) {
        if(window == null) return
        window.x = x.toDouble()
        window.y = y.toDouble()
        window.width = width.toDouble()
        window.height = height.toDouble()
        BoundaryChecker.reset(window)
    }
    fun bind(dialog: Dialog<*>?) {
        if(dialog == null) return
        dialog.x = x.toDouble()
        dialog.y = y.toDouble()
        dialog.width = width.toDouble()
        dialog.height = height.toDouble()
        BoundaryChecker.reset(dialog)
    }
}