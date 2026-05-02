package io.github.nayasis.kotlin.javafx.stage

import javafx.collections.ListChangeListener
import javafx.css.PseudoClass
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.Label
import javafx.scene.control.MenuBar
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.stage.Screen
import javafx.stage.Stage
import javafx.stage.Window

private val PSEUDO_INACTIVE = PseudoClass.getPseudoClass("inactive")
private const val STYLESHEET = "basicafx/css/window-header.css"

class WindowHeaderHelper(
    private val titleBar: HBox,
) {

    private var dragOffsetX = 0.0
    private var dragOffsetY = 0.0
    private var dragRestoreRatioX = 0.5
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
        findMenuBar()?.apply {
            minWidth = Region.USE_COMPUTED_SIZE
            prefWidth = Region.USE_COMPUTED_SIZE
            maxWidth = Region.USE_PREF_SIZE
            styleClass.add("window-menu-bar")
            headerMenuBar = this
        }

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
            }.also { headerTitle = it }
        ).apply {
            alignment = Pos.CENTER_LEFT
            padding = Insets(0.0, 0.0, 0.0, 0.0)
            HBox.setMargin(this, Insets(0.0, 8.0, 0.0, 0.0))
            registerWindowDrag(this)
        }

        val centerSpacer = Region().apply {
            minWidth = 120.0
            registerWindowDrag(this)
        }

        titleBar.children.add(0, titleNode)
        titleBar.children.add(centerSpacer)
        HBox.setHgrow(centerSpacer, Priority.ALWAYS)
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

    private fun syncStageIcon(stage: Stage) {
        headerIcon?.image = stage.icons.firstOrNull()
    }

    private fun registerWindowDrag(node: Node) {
        node.setOnMousePressed { event ->
            withDragStage(node, event) { stage ->
                if (stage.isMaximized) {
                    rememberRestoreAnchor(event)
                } else {
                    dragOffsetX = event.screenX - stage.x
                    dragOffsetY = event.screenY - stage.y
                }
            }
        }

        node.setOnMouseDragged { event ->
            withDragStage(node, event) { stage ->
                if (stage.isMaximized) {
                    restoreAndMove(stage, event)
                } else {
                    stage.x = event.screenX - dragOffsetX
                    stage.y = event.screenY - dragOffsetY
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
        dragOffsetY = event.sceneY.coerceAtLeast(0.0)
    }

    private fun restoreAndMove(stage: Stage, event: MouseEvent) {
        stage.isMaximized = false
        dragOffsetX = stage.width * dragRestoreRatioX
        stage.x = event.screenX - dragOffsetX
        stage.y = event.screenY - dragOffsetY
    }
}
