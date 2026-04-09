@file:Suppress("unused")

package io.github.nayasis.kotlin.javafx.stage

import io.github.nayasis.kotlin.basica.core.extension.FieldProperty
import io.github.nayasis.kotlin.javafx.animation.toFxDuration
import io.github.nayasis.kotlin.javafx.property.InsetProperty
import io.github.nayasis.kotlin.javafx.scene.*
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.geometry.Rectangle2D
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Dialog
import javafx.stage.Stage
import javafx.stage.Window
import javafx.stage.WindowEvent
import tornadofx.runLater
import java.io.Serializable
import kotlin.time.Duration

class Stages { companion object {

    val defaultIcons = IconContainer()

    val focusedWindow: Window?
        get() {
            for( window in windows)
                if( window.isFocused ) return window
            return null
        }

    val windows: List<Window>
        get() = Window.getWindows()

}}

fun Stage.loadDefaultIcon(): Stage {
    if( ! Stages.defaultIcons.isEmpty() ) {
        this.icons.addAll(Stages.defaultIcons.icons)
    }
    return this
}

val Stage.isBorderless: Boolean
    get() = scene?.isBorderless() ?: false

fun Stage.setBorderless(option: Stage.() -> Unit = {}, defaultCss: Boolean = true) {
    scene?.setBorderless(defaultCss = defaultCss)
    this.apply(option)
}

fun Stage.addConstraintRetainer() {
    scene?.addConstraintRetainer()
}

fun Stage.addResizeHandler() {
    scene?.addResizeHandler()
}

fun Stage.addMoveHandler(node: Node, buttonClose: Boolean = false, buttonHide: Boolean = false, buttonZoom: Boolean = false, buttonAll: Boolean = false) {
    scene?.addMoveHandler(node = node, buttonClose = buttonClose, buttonHide = buttonHide, buttonZoom = buttonZoom, buttonAll = buttonAll)
}

fun Stage.addClose(button: Button) {
    scene?.addClose(button)
}

fun Stage.addIconified(button: Button) {
    scene?.addIconified(button)
}

fun Stage.addZoomed(button: Button) {
    scene?.addZoomed(button)
}

fun <T> Stage.hideWhile(
    delay: Duration = Duration.ZERO,
    block: () -> T,
): T {

    val prevImplicit = Platform.isImplicitExit()

    Platform.setImplicitExit(false)
    hide()

    return try {
        block()
    } finally {
        runLater(delay.toFxDuration()) {
            try {
                show()
                requestFocus()
            } finally {
                Platform.setImplicitExit(prevImplicit)
            }
        }
    }
}

val Stage.isZoomed: Boolean
    get() = scene?.isZoomed() ?: false

fun Stage.setZoom(enable: Boolean) {
    scene?.setZoom(enable)
}

fun Stage.addCloseRequest(event: EventHandler<WindowEvent>) =
    this.addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST,event)

val Stage.focusedNode: Node?
    get() = this.scene?.focusOwnerProperty()?.get()

val Window.boundary: Rectangle2D
    get() = Rectangle2D(this.x, this.y, this.width, this.height)

val Scene.boundary: Rectangle2D
    get() = Rectangle2D(this.x, this.y, this.width, this.height)

val Dialog<*>.boundary: Rectangle2D
    get() = Rectangle2D(this.x, this.y, this.width, this.height)

val Scene?.stage: Stage?
    get() = this?.window as Stage?


/**
 * Previous inset property before window maximized.
 *
 * It is activated by [Stage.watchMaximized]
 */
var Stage.previousBoundary: MaximizedProperty by FieldProperty{MaximizedProperty()}
private var Stage.maximizedWatcherInstalled: Boolean by FieldProperty{false}

/**
 * Manage previous window inset (x,y,width,height) when maximized property is changed.
 *
 * Previous inset property is [Stage.previousBoundary]
 */
fun Stage.watchMaximized() {
    if(maximizedWatcherInstalled) return
    maximizedWatcherInstalled = true

    capturePreviousBoundary()
    var captureSequence = 0L
    val scheduleCapture = {
        captureSequence += 1
        val expected = captureSequence
        Platform.runLater {
            if(expected != captureSequence) return@runLater
            if(!isMaximized && isNormalBoundsCandidate()) {
                capturePreviousBoundary()
            }
        }
    }
    maximizedProperty().addListener { _, _, maximized ->
        previousBoundary.maximized = maximized
    }
    xProperty().addListener { _, _, _ ->
        if(!isMaximized) {
            scheduleCapture()
        }
    }
    yProperty().addListener { _, _, _ ->
        if(!isMaximized) {
            scheduleCapture()
        }
    }
    widthProperty().addListener { _, _, _ ->
        if(!isMaximized) {
            scheduleCapture()
        }
    }
    heightProperty().addListener { _, _, _ ->
        if(!isMaximized) {
            scheduleCapture()
        }
    }
}

private fun Stage.capturePreviousBoundary() {
    previousBoundary.boundary.x = x.toInt()
    previousBoundary.boundary.y = y.toInt()
    if(width > 0.0) {
        previousBoundary.boundary.width = width.toInt()
    }
    if(height > 0.0) {
        previousBoundary.boundary.height = height.toInt()
    }
}

private fun Stage.isNormalBoundsCandidate(): Boolean {
    val screen = BoundaryChecker().getMajorScreen(this).visualBounds
    val tolerance = 16.0

    val fillsScreen =
        width >= screen.width - tolerance &&
        height >= screen.height - tolerance &&
        x <= screen.minX + tolerance &&
        y <= screen.minY + tolerance

    return !fillsScreen
}

class MaximizedProperty: Serializable {
    var maximized = false
    var boundary = InsetProperty()
    fun bind(stage: Stage?) {
        if(stage == null) return
        if(maximized) {
            boundary.bind(stage)
            stage.isMaximized = maximized
        }
    }
}

