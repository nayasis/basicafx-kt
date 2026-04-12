@file:Suppress("unused")

package io.github.nayasis.kotlin.javafx.stage

import io.github.nayasis.kotlin.basica.core.extension.FieldProperty
import io.github.nayasis.kotlin.javafx.animation.toFxDuration
import io.github.nayasis.kotlin.javafx.property.InsetProperty
import io.github.nayasis.kotlin.javafx.scene.addClose
import io.github.nayasis.kotlin.javafx.scene.addConstraintRetainer
import io.github.nayasis.kotlin.javafx.scene.addIconified
import io.github.nayasis.kotlin.javafx.scene.addMoveHandler
import io.github.nayasis.kotlin.javafx.scene.addResizeHandler
import io.github.nayasis.kotlin.javafx.scene.addZoomed
import io.github.nayasis.kotlin.javafx.scene.isBorderless
import io.github.nayasis.kotlin.javafx.scene.isZoomed
import io.github.nayasis.kotlin.javafx.scene.setBorderless
import io.github.nayasis.kotlin.javafx.scene.setZoom
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
        get() = windows.firstOrNull { it.isFocused }

    val windows: List<Window>
        get() = Window.getWindows()

}}

fun Stage.loadDefaultIcon(): Stage = apply {
    if( ! Stages.defaultIcons.isEmpty() ) {
        icons.addAll(Stages.defaultIcons.icons)
    }
}

val Stage.isBorderless: Boolean
    get() = scene?.isBorderless() ?: false

fun Stage.setBorderless(option: Stage.() -> Unit = {}, defaultCss: Boolean = true) {
    scene?.setBorderless(defaultCss = defaultCss)
    apply(option)
}

fun Stage.addConstraintRetainer() = scene?.addConstraintRetainer()

fun Stage.addResizeHandler() = scene?.addResizeHandler()

fun Stage.addMoveHandler(node: Node, buttonClose: Boolean = false, buttonHide: Boolean = false, buttonZoom: Boolean = false, buttonAll: Boolean = false) =
    scene?.addMoveHandler(node = node, buttonClose = buttonClose, buttonHide = buttonHide, buttonZoom = buttonZoom, buttonAll = buttonAll)

fun Stage.addClose(button: Button) = scene?.addClose(button)

fun Stage.addIconified(button: Button) = scene?.addIconified(button)

fun Stage.addZoomed(button: Button) = scene?.addZoomed(button)

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

fun Stage.setZoom(enable: Boolean) = scene?.setZoom(enable)

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

    previousBoundary.capture(this)
    var captureToken = 0L
    val scheduleCapture = {
        val expected = ++captureToken
        Platform.runLater {
            if(expected == captureToken && !isMaximized && isNormalBoundsCandidate()) {
                previousBoundary.capture(this)
            }
        }
    }

    maximizedProperty().addListener { _, _, maximized ->
        previousBoundary.maximized = maximized
    }
    listOf(xProperty(), yProperty(), widthProperty(), heightProperty()).forEach { property ->
        property.addListener {
            if(!isMaximized) scheduleCapture()
        }
    }
}

internal fun Stage.fillsCurrentScreen(inset: InsetProperty = InsetProperty(this), tolerance: Double = 16.0): Boolean {
    val screen = BoundaryChecker().getMajorScreen(this).visualBounds
    return inset.width >= screen.width - tolerance &&
        inset.height >= screen.height - tolerance &&
        inset.x <= screen.minX + tolerance &&
        inset.y <= screen.minY + tolerance
}

private fun Stage.isNormalBoundsCandidate(): Boolean = !fillsCurrentScreen()

class MaximizedProperty: Serializable {
    var maximized = false
    var boundary = InsetProperty()
    fun capture(stage: Stage) {
        boundary.apply {
            x = stage.x.toInt()
            y = stage.y.toInt()
            stage.width. toInt().takeIf { it > 0 }?.let { width  = it }
            stage.height.toInt().takeIf { it > 0 }?.let { height = it }
        }
    }
}

