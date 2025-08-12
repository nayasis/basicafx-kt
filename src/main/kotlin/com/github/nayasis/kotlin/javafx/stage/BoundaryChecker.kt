package com.github.nayasis.kotlin.javafx.stage

import io.github.oshai.kotlinlogging.KotlinLogging
import javafx.geometry.Rectangle2D
import javafx.scene.Scene
import javafx.scene.control.Dialog
import javafx.stage.Screen
import javafx.stage.Window
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private val logger = KotlinLogging.logger {}

@Suppress("MemberVisibilityCanBePrivate")
class BoundaryChecker {

    /**
     * reset stage position to displayed screen
     *
     * @param window Window
     */
    @Suppress("DuplicatedCode")
    fun reset(window: Window) {
        if( isShownOnScreen(window) ) return
        Screen.getPrimary().visualBounds.let {
            window.x = it.minX
            window.y = it.minY
            window.width  = min(window.width,  it.width)
            window.height = min(window.height, it.height)
        }
    }

    /**
     * reset stage position to displayed screen
     *
     * @param dialog Window
     */
    @Suppress("DuplicatedCode")
    fun reset(dialog: Dialog<*>) {
        if( isShownOnScreen(dialog) ) return
        Screen.getPrimary().visualBounds.let {
            dialog.x = it.minX
            dialog.y = it.minY
            dialog.width  = min(dialog.width,  it.width)
            dialog.height = min(dialog.height, it.height)
        }
    }

    fun isShownOnScreen(window: Window): Boolean =
        isShownOnScreen(window.boundary)

    fun isShownOnScreen(dialog: Dialog<*>): Boolean =
        isShownOnScreen(dialog.boundary)

    fun isShownOnScreen(rectangle: Rectangle2D): Boolean =
        Screen.getScreensForRectangle(rectangle).isNotEmpty()

    /**
     * get screen which has maximum staged area potion.
     *
     * if there is no screen related to stage, return primary screen.
     */
    fun getMajorScreen(window: Window): Screen =
        getMajorScreen(window.boundary)

    /**
     * get screen which has maximum staged area potion.
     *
     * if there is no screen related to stage, return primary screen.
     */
    fun getMajorScreen(scene: Scene): Screen =
        getMajorScreen(scene.boundary)

    /**
     * get screen which has maximum staged area potion.
     *
     * if there is no screen related to stage, return primary screen.
     */
    fun getMajorScreen(boundary: Rectangle2D): Screen {
        var major = Screen.getPrimary()
        var max   = 0.0
        for( screen in Screen.getScreens() ) {
            val area = screen.bounds.getIntersectedArea(boundary)
            if( area > max ) {
                major = screen
                max = area
            }
        }
        return major
    }

}

fun Rectangle2D.getIntersectedArea(r: Rectangle2D): Double {
    return when {
        this.contains(r) -> this.width * this.height
        this.intersects(r) -> {
            val minX = max(this.minX,r.minX)
            val maxX = min(this.maxX,r.maxX)
            val minY = max(this.minY,r.minY)
            val maxY = min(this.maxY,r.maxY)
            abs(minX - maxX) * abs(minY - maxY)
        }
        else -> 0.0
    }
}