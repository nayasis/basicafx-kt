package com.github.nayasis.kotlin.javafx.stage

import com.github.nayasis.kotlin.basica.core.extention.FieldProperty
import com.github.nayasis.kotlin.javafx.property.InsetProperty
import com.github.nayasis.kotlin.javafx.scene.*
import javafx.event.EventHandler
import javafx.geometry.Rectangle2D
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Dialog
import javafx.stage.Stage
import javafx.stage.Window
import javafx.stage.WindowEvent
import mu.KotlinLogging
import java.io.Serializable

private val logger = KotlinLogging.logger {}

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

/**
 * Manage previous window inset (x,y,width,height) when maximized property is changed.
 *
 * Previous inset property is [Stage.previousBoundary]
 */
fun Stage.watchMaximized() {
    maximizedProperty().addListener { _, _, maximized ->
        if(maximized) {
            previousBoundary.boundary.width = this.width.toInt()
            previousBoundary.boundary.height = this.height.toInt()
            previousBoundary.maximized = true
        } else {
            previousBoundary.boundary.width = 0
            previousBoundary.boundary.height = 0
            previousBoundary.maximized = false
        }
    }
    xProperty().addListener { _, x, _ ->
        if(!isMaximized) {
            previousBoundary.boundary.x = x.toInt()
        }
    }
    yProperty().addListener { _, y, _ ->
        if(!isMaximized) {
            previousBoundary.boundary.y = y.toInt()
        }
    }
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