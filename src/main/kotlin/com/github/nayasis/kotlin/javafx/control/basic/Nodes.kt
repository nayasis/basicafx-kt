@file:Suppress("unused")

package com.github.nayasis.kotlin.javafx.control.basic

import com.github.nayasis.kotlin.javafx.model.Point
import com.github.nayasis.kotlin.javafx.stage.stage
import javafx.event.EventHandler
import javafx.event.EventTarget
import javafx.geometry.Insets
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.input.KeyEvent
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox

fun Node.repack() =
    this.managedProperty().bind(this.visibleProperty())

val Node.root: Node
    get() {
        var curr = this
        while( true ) {
            if( curr.parent == null ) return curr
            curr = curr.parent
        }
    }

val EventTarget.children: List<EventTarget>
    get() {
        return when(this) {
            is SplitPane -> items
            is ScrollPane -> content?.children ?: emptyList()
            is ToolBar -> items
            is Tab -> listOf(content)
            is TabPane -> tabs
            is TableView<*> -> columns
            is TableColumn<*,*> -> columns
            is MenuBar -> menus
            is Menu -> items
            is MenuItem -> emptyList()
            is HBox -> children
            is VBox -> children
            is Pane -> children
            is Group -> children
            is Control -> {
                try {
                    (skin as? SkinBase<*>)?.children ?: emptyList()
                } catch (e: Exception) {
                    emptyList()
                }
            }
            else -> emptyList()
        }
    }

val EventTarget.allChildren: List<EventTarget>
    get() = HashSet<EventTarget>().let{
        findChildren(this,it)
        it.remove(this)
        it
    }.toList()

val Node.allParents: List<Node>
    get() = ArrayList<Node>().let{
        findParent(this.parent,it)
        it
    }

val EventTarget.fxId: String
    get() = when (this) {
        is Pane -> this.id
        is TabPane -> this.id
        is TableColumnBase<*,*> -> this.id
        is MenuItem -> this.id
        is Tab -> this.id
        is Node -> this.id
        else -> null
    } ?: ""

val EventTarget.allChildrenById: Map<String,EventTarget>
    get() = this.allChildren.filter { it.fxId.isNotEmpty() }.associateBy { it.fxId }

val Node.allParentsById: Map<String,Node>
    get() = this.allParents.filter { it.fxId.isNotEmpty() }.associateBy { it.fxId }

var Node.leftAnchor: Double?
    get() = AnchorPane.getLeftAnchor(this)
    set(value) = AnchorPane.setLeftAnchor(this, value)

var Node.rightAnchor: Double?
    get() = AnchorPane.getRightAnchor(this)
    set(value) = AnchorPane.setRightAnchor(this, value)

var Node.bottomAnchor: Double?
    get() = AnchorPane.getBottomAnchor(this)
    set(value) = AnchorPane.setBottomAnchor(this, value)

var Node.topAnchor: Double?
    get() = AnchorPane.getTopAnchor(this)
    set(value) = AnchorPane.setTopAnchor(this, value)

/**
 * remove all anchor pane constraints
 */
fun Node.clearAnchor() {
    this.leftAnchor   = null
    this.rightAnchor  = null
    this.bottomAnchor = null
    this.topAnchor    = null
}

private fun findParent(node: Node?, list: ArrayList<Node>) {
    node?.let {
        list.add(node)
        node.parent?.let { findParent(it, list) }
    }
}

private fun findChildren(node: EventTarget?, set: HashSet<EventTarget>) {
    node?.let {
        set.add(node)
        node.children.forEach { child -> findChildren(child,set) }
    }
}

fun Node.addKeyPressed(event: EventHandler<in KeyEvent>) {
    this.addEventFilter(KeyEvent.KEY_PRESSED,event)
}

fun Node.addKeyReleased(event: EventHandler<in KeyEvent>) {
    this.addEventFilter(KeyEvent.KEY_RELEASED,event)
}

var Node.vmargin: Insets
    get() = VBox.getMargin(this)
    set(value) = VBox.setMargin(this, value)

var Node.hmargin: Insets
    get() = HBox.getMargin(this)
    set(value) = HBox.setMargin(this, value)


// style extention

val Node.allStyles: Map<String,String>
    get() = this.style.split(";").mapNotNull {
        val words = it.split(":")
        when {
            words.size >= 2 -> if(words[1].isEmpty()) null else words[0] to words[1]
            else -> null
        }
    }.toMap()

fun Node.setStyle(style: Map<String,String?>) =
    style.filter { ! it.value.isNullOrEmpty() }.map{ "${it.key}:${it.value}" }.joinToString(";").let { this.style = it }

fun Node.getStyle(key: String): String? = this.allStyles[key]

fun Node.setStyle(key: String, value: String?) {
    this.allStyles.toMutableMap().let {
        if(value.isNullOrEmpty()) {
            it.remove(key)
        } else {
            it[key] = value
        }
        setStyle(it)
    }
}

fun Node.removeStyle(key: String) {
    setStyle(key,null)
}

fun Node.setMoveHandler(styleClassOnDragged: String? = null) {
    val stage = this.scene.stage
    val offset = Point()
    setOnMousePressed { e ->
        offset.x = e.sceneX
        offset.y = e.sceneY
    }
    setOnMouseDragged { e ->
        stage?.x = e.screenX - offset.x
        stage?.y = e.screenY - offset.y
        styleClassOnDragged?.let {
            if( it !in styleClass )
                styleClass.add(it)
        }
    }
    setOnMouseReleased {
        styleClassOnDragged?.let {
            styleClass.remove(it)
        }
    }
}