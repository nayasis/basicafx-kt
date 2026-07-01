package io.github.nayasis.kotlin.javafx.stage

import io.github.nayasis.kotlin.javafx.control.basic.keepPrefHeight
import io.github.nayasis.kotlin.javafx.model.Point
import javafx.collections.ListChangeListener
import javafx.css.PseudoClass
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.Label
import javafx.scene.control.MenuBar
import javafx.scene.control.OverrunStyle
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.Cursor
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.shape.Rectangle
import javafx.stage.Screen
import javafx.stage.Stage
import javafx.stage.Window

private val PSEUDO_INACTIVE = PseudoClass.getPseudoClass("inactive")
private const val STYLESHEET = "basicafx/css/window-header.css"
private const val TOP_RESIZE_HEIGHT = 5.0

class WindowHeaderHelper(
    private val titleBar: HBox,
) {

    private val dragOffset = Point()
    private var dragRestoreRatioX = 0.5
    private val resizeStart = Point()
    private var resizeStartHeight = 0.0
    private var topResizing = false
    private var headerMenuBar: MenuBar? = null
    private var headerIcon: ImageView? = null
    private var headerTitle: Label? = null
    private var stageBound = false

    init {
        install()
    }

    private fun install() {
        if (STYLESHEET !in titleBar.stylesheets) {
            titleBar.stylesheets.add(STYLESHEET)
        }
        titleBar.keepPrefHeight()
        findMenuBar()?.let { installMenuBar(it) }

        val titleNode = HBox(
            6.0,
            ImageView().apply {
                fitHeight = 16.0
                fitWidth = 16.0
                isPickOnBounds = true
                isPreserveRatio = true
                HBox.setMargin(this, Insets(0.0, 0.0, 0.0, 4.0))
            }.also { headerIcon = it },
            Label().apply {
                styleClass.add("window-header-title")
                isWrapText = false
                textOverrun = OverrunStyle.CLIP
                minWidth = Region.USE_PREF_SIZE
            }.also { headerTitle = it }
        ).apply {
            alignment = Pos.CENTER_LEFT
            padding = Insets(0.0, 0.0, 0.0, 0.0)
            HBox.setMargin(this, Insets(0.0, 8.0, 0.0, 0.0))
            minWidth = Region.USE_PREF_SIZE
            keepPrefHeight()
            registerWindowDrag(this)
        }

        val centerSpacer = Region().apply {
            minWidth = 0.0
            prefWidth = 120.0
            registerWindowDrag(this)
        }

        titleBar.children.add(0, titleNode)
        titleBar.children.add(centerSpacer)
        HBox.setHgrow(centerSpacer, Priority.ALWAYS)
        registerTopResize(titleBar)
        bindToStageWhenReady()
    }

    private fun findMenuBar(): MenuBar? {
        titleBar.children.forEach { child ->
            if (child is MenuBar) {
                return child
            }
            if (child is Parent) {
                child.lookupAll(".menu-bar").firstOrNull()?.let { found ->
                    return found as? MenuBar
                }
            }
        }
        return null
    }

    private fun installMenuBar(menuBar: MenuBar) {
        menuBar.apply {
            minWidth = Region.USE_PREF_SIZE
            prefWidth = Region.USE_COMPUTED_SIZE
            keepPrefHeight()
            styleClass.add("window-menu-bar")
        }
        headerMenuBar = menuBar

        val index = titleBar.children.indexOf(menuBar)
        if (index < 0) {
            return
        }

        titleBar.children.removeAt(index)
        titleBar.children.add(index, Pane(menuBar).apply {
            minWidth = 0.0
            prefWidth = Region.USE_COMPUTED_SIZE
            keepPrefHeight()
            clipToBounds(this)
            HBox.setHgrow(this, Priority.SOMETIMES)
        })
    }

    private fun bindToStageWhenReady() {
        titleBar.sceneProperty().addListener { _, _, scene ->
            scene?.windowProperty()?.addListener { _, _, window ->
                bindToStage(window)
            }
            bindToStage(scene?.window)
        }
        titleBar.scene?.windowProperty()?.addListener { _, _, window ->
            bindToStage(window)
        }
        bindToStage(titleBar.scene?.window)
    }

    private fun bindToStage(window: Window?) {
        val stage = window as? Stage ?: return
        if (stageBound) {
            return
        }
        stageBound = true
        headerTitle?.text = stage.title
        stage.titleProperty().addListener { _, _, newTitle ->
            headerTitle?.text = newTitle
        }
        headerTitle?.pseudoClassStateChanged(PSEUDO_INACTIVE, !stage.isFocused)
        headerMenuBar?.pseudoClassStateChanged(PSEUDO_INACTIVE, !stage.isFocused)
        stage.focusedProperty().addListener { _, _, focused ->
            headerTitle?.pseudoClassStateChanged(PSEUDO_INACTIVE, focused != true)
            headerMenuBar?.pseudoClassStateChanged(PSEUDO_INACTIVE, focused != true)
        }
        syncStageIcon(stage)
        stage.icons.addListener(ListChangeListener<Image> {
            syncStageIcon(stage)
        })
    }

    private fun clipToBounds(region: Region) {
        region.clip = Rectangle().apply {
            widthProperty().bind(region.widthProperty())
            heightProperty().bind(region.heightProperty())
        }
    }

    private fun syncStageIcon(stage: Stage) {
        headerIcon?.image = stage.icons.firstOrNull()
    }

    private fun registerTopResize(node: Node) {
        node.addEventFilter(MouseEvent.MOUSE_MOVED) { event ->
            if (event.isInTopResizeArea()) {
                node.scene.cursor = Cursor.N_RESIZE
            } else if (node.scene.cursor == Cursor.N_RESIZE) {
                node.scene.cursor = Cursor.DEFAULT
            }
        }

        node.addEventFilter(MouseEvent.MOUSE_EXITED) {
            if (node.scene.cursor == Cursor.N_RESIZE) {
                node.scene.cursor = Cursor.DEFAULT
            }
        }

        node.addEventFilter(MouseEvent.MOUSE_PRESSED) { event ->
            val stage = node.scene?.window as? Stage ?: return@addEventFilter
            if (!event.isInTopResizeArea() || stage.isMaximized) {
                return@addEventFilter
            }
            resizeStart.x = stage.y
            resizeStart.y = event.screenY
            resizeStartHeight = stage.height
            topResizing = true
            node.scene.cursor = Cursor.N_RESIZE
            event.consume()
        }

        node.addEventFilter(MouseEvent.MOUSE_DRAGGED) { event ->
            val stage = node.scene?.window as? Stage ?: return@addEventFilter
            if (!topResizing || stage.isMaximized) {
                return@addEventFilter
            }
            resizeTop(stage, event.screenY)
            event.consume()
        }

        node.addEventFilter(MouseEvent.MOUSE_RELEASED) {
            topResizing = false
            if (node.scene.cursor == Cursor.N_RESIZE) {
                node.scene.cursor = Cursor.DEFAULT
            }
        }
    }

    private fun MouseEvent.isInTopResizeArea(): Boolean =
        y in 0.0..TOP_RESIZE_HEIGHT

    private fun resizeTop(stage: Stage, screenY: Double) {
        val delta = screenY - resizeStart.y
        val nextHeight = (resizeStartHeight - delta).coerceAtLeast(stage.minHeight)
        val appliedDelta = resizeStartHeight - nextHeight
        stage.y = resizeStart.x + appliedDelta
        stage.height = nextHeight
    }

    private fun registerWindowDrag(node: Node) {
        node.setOnMousePressed { event ->
            withDragStage(node, event) { stage ->
                if (stage.isMaximized) {
                    rememberRestoreAnchor(event)
                } else {
                    dragOffset.x = event.screenX - stage.x
                    dragOffset.y = event.screenY - stage.y
                }
            }
        }

        node.setOnMouseDragged { event ->
            withDragStage(node, event) { stage ->
                if (stage.isMaximized) {
                    restoreAndMove(stage, event)
                } else {
                    stage.x = event.screenX - dragOffset.x
                    stage.y = event.screenY - dragOffset.y
                }
            }
        }

        node.setOnMouseClicked { event ->
            (node.scene?.window as? Stage)
                ?.takeIf { event.clickCount == 2 }
                ?.let { it.isMaximized = !it.isMaximized }
        }
    }

    private inline fun withDragStage(node: Node, event: MouseEvent, block: (Stage) -> Unit) {
        if (!event.isPrimaryButtonDown) {
            return
        }
        (node.scene?.window as? Stage)?.let(block)
    }

    private fun rememberRestoreAnchor(event: MouseEvent) {
        val screenBounds = Screen.getScreensForRectangle(event.screenX, event.screenY, 1.0, 1.0)
            .firstOrNull()
            ?.visualBounds
            ?: Screen.getPrimary().visualBounds
        dragRestoreRatioX = ((event.screenX - screenBounds.minX) / screenBounds.width).coerceIn(0.0, 1.0)
        dragOffset.y = event.sceneY.coerceAtLeast(0.0)
    }

    private fun restoreAndMove(stage: Stage, event: MouseEvent) {
        stage.isMaximized = false
        dragOffset.x = stage.width * dragRestoreRatioX
        stage.x = event.screenX - dragOffset.x
        stage.y = event.screenY - dragOffset.y
    }
}
