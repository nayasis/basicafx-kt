package io.github.nayasis.kotlin.javafx.property

import io.github.nayasis.kotlin.javafx.misc.Desktop
import io.github.nayasis.kotlin.javafx.model.Point
import io.github.nayasis.kotlin.javafx.stage.BoundaryChecker
import javafx.geometry.Rectangle2D
import javafx.scene.control.Dialog
import javafx.stage.Window
import java.awt.GraphicsConfiguration
import java.awt.Rectangle
import java.io.Serializable
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class InsetProperty(
    var x: Int      = 100,
    var y: Int      = 100,
    var width: Int  = 500,
    var height: Int = 600,
): Serializable {

    constructor(window: Window): this(
        x      = window.x.toInt(),
        y      = window.y.toInt(),
        width  = window.width.toInt(),
        height = window.height.toInt(),
    )

    constructor(dialog: Dialog<*>): this(
        x      = dialog.x.toInt(),
        y      = dialog.y.toInt(),
        width  = dialog.width.toInt(),
        height = dialog.height.toInt(),
    )

    constructor(boundary: Rectangle2D): this (
        x      = boundary.minX.toInt(),
        y      = boundary.minY.toInt(),
        width  = boundary.width.toInt(),
        height = boundary.height.toInt(),
    )

    constructor(boundary: Rectangle): this (
        x      = boundary.minX.toInt(),
        y      = boundary.minY.toInt(),
        width  = boundary.width,
        height = boundary.height,
    )

    constructor(configuration: GraphicsConfiguration): this() {
        val insets = Desktop.toolkit.getScreenInsets(configuration)
        val bounds = configuration.bounds
        x      = bounds.x + insets.left
        y      = bounds.y + insets.top
        width  = bounds.width  - (insets.left + insets.right)
        height = bounds.height - (insets.top + insets.bottom)
    }

    fun read(window: Window?) {
        if(window == null) return
        x      = window.x.toInt()
        y      = window.y.toInt()
        width  = window.width.toInt()
        height = window.height.toInt()
    }

    fun bind(window: Window?) {
        if(window == null) return
        window.x = x.toDouble()
        window.y = y.toDouble()
        window.width = width.toDouble()
        window.height = height.toDouble()
        BoundaryChecker().reset(window)
    }

    fun bind(dialog: Dialog<*>?) {
        if(dialog == null) return
        dialog.x = x.toDouble()
        dialog.y = y.toDouble()
        dialog.width = width.toDouble()
        dialog.height = height.toDouble()
        BoundaryChecker().reset(dialog)
    }

    operator fun contains(p: Point?): Boolean {
        return if(p == null) false else
            x <= p.x && p.x <= (x+width) &&
            y <= p.y && p.y <= (y+height)
    }

    operator fun contains(r: InsetProperty?): Boolean {
        return if (r == null) false else r.x >= x && r.y >= y && r.x <= (x+width) && r.y <= (y+height)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun intersects(r: InsetProperty?): Boolean {
        return if (r == null) false else (r.x+r.width) > x && (r.y+r.height) > y && r.x < (x+width) && r.y < (y+height)
    }

    fun toRectangle2D(): Rectangle2D =
        Rectangle2D(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble())

    fun getIntersectedArea(r: InsetProperty): Int {
        return when {
            contains(r) -> r.width * r.height
            intersects(r) -> {
                val minX = max(x, r.x)
                val maxX = min(x+width,r.x + r.width)
                val minY = max(y, r.y)
                val maxY = min(y + height,r.y + r.height)
                abs(minX - maxX) * abs(minY - maxY)
            }
            else -> 0
        }
    }

}