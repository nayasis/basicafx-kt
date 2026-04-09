@file:Suppress("unused")

package io.github.nayasis.kotlin.javafx.property

import com.fasterxml.jackson.annotation.JsonIgnore
import io.github.nayasis.kotlin.javafx.control.basic.allChildren
import io.github.nayasis.kotlin.javafx.control.basic.fxId
import io.github.nayasis.kotlin.javafx.scene.previousZoomInset
import io.github.nayasis.kotlin.javafx.stage.fillsCurrentScreen
import io.github.nayasis.kotlin.javafx.stage.MaximizedProperty
import io.github.nayasis.kotlin.javafx.stage.previousBoundary
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.event.EventTarget
import javafx.scene.control.CheckBox
import javafx.scene.control.CheckMenuItem
import javafx.scene.control.ChoiceBox
import javafx.scene.control.ComboBox
import javafx.scene.control.Control
import javafx.scene.control.TableView
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.control.TextInputControl
import javafx.scene.layout.Pane
import javafx.stage.Stage
import javafx.stage.WindowEvent
import org.controlsfx.control.CheckComboBox
import java.io.Serializable
import kotlin.math.max
import kotlin.math.min
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf

data class StageProperty(
    var inset: InsetProperty?                  = null,
    var maximized: Boolean                     = false,
    var previousZoomInset: InsetProperty?      = null,
    var previousBoundary: MaximizedProperty?   = null,
    var tables: HashMap<String,TableProperty>? = null,
    var checks: HashMap<String,Boolean>?       = null,
    var values: HashMap<String,String>?        = null,
    var lists: HashMap<String,List<Int>>?      = null,
    var visibles: HashMap<String,Boolean>?     = null,
    var editables: HashMap<String,Boolean>?    = null,
    var disables: HashMap<String,Boolean>?     = null,
    var indices: HashMap<String,Int>?          = null,
): Serializable{

    private data class WindowStateSnapshot(
        val maximized: Boolean,
        val hasNormalBoundary: Boolean,
        val normalBoundary: InsetProperty,
    )

    @JsonIgnore
    var includeKlass = ArrayList<KClass<out EventTarget>>()
    @JsonIgnore
    var excludeKlass = ArrayList<KClass<out EventTarget>>()
    @JsonIgnore
    var includeId = HashSet<String>()
    @JsonIgnore
    var excludeId = HashSet<String>()
    @JsonIgnore
    val includeObject = HashSet<EventTarget>()
    @JsonIgnore
    val excludeObject = HashSet<EventTarget>()

    constructor(stage: Stage, includeChildren: Boolean = true): this() {
        read(stage,includeChildren)
    }

    fun read(stage: Stage, includeChildren: Boolean = true) {

        tables    = hashMapOf()
        checks    = hashMapOf()
        values    = hashMapOf()
        lists     = hashMapOf()
        visibles  = hashMapOf()
        editables = hashMapOf()
        disables  = hashMapOf()
        indices   = hashMapOf()

        val currentInset = InsetProperty(stage)
        val snapshot = readWindowState(stage, currentInset)

        inset = currentInset
        maximized = snapshot.maximized
        previousZoomInset = stage.scene.previousZoomInset
        previousBoundary = MaximizedProperty().apply {
            maximized = snapshot.maximized && snapshot.hasNormalBoundary
            boundary = snapshot.normalBoundary
        }

        if(!includeChildren) return

        getAllChildren(stage).forEach {
            if( skippable(it) ) return@forEach
            when(it) {
                is TableView<*>     -> tables?.set(it.id, TableProperty(it))
                is CheckMenuItem    -> checks?.set(it.id, it.isSelected)
                is CheckBox         -> checks?.set(it.id, it.isSelected)
                is TextField        -> values?.set(it.id, it.text ?: "")
                is TextArea         -> values?.set(it.id, it.text ?: "")
                is ComboBox<*>      -> indices?.set(it.id, it.selectionModel.selectedIndex)
                is ChoiceBox<*>     -> indices?.set(it.id, it.selectionModel.selectedIndex)
                is CheckComboBox<*> -> lists?.set(it.id, it.checkModel.checkedIndices.toList())
            }
            when(it) {
                is Pane -> visibles?.set(it.id, it.isVisible)
                is Control -> {
                    visibles?.set(it.id, it.isVisible)
                    disables?.set(it.id, it.isDisable)
                    if (it is TextInputControl) {
                        editables?.set(it.id, it.isEditable)
                    }
                }
            }
        }

    }

    @Suppress("UNCHECKED_CAST")
    fun bind(stage: Stage?, visibility: Boolean = true, includeChildren: Boolean = true) {

        if( stage?.scene == null ) return

        bindWindowState(stage)
        previousZoomInset?.let { stage.scene.previousZoomInset = it }

        if(!includeChildren) return

        getAllChildren(stage).forEach {
            if( skippable(it) ) return@forEach
            when(it) {
                is TableView<*>     -> tables?.get(it.id)?.bind(it as TableView<Any>)
                is CheckMenuItem    -> checks?.get(it.id)?.let{ value -> it.isSelected = value }
                is CheckBox         -> checks?.get(it.id)?.let{ value -> it.isSelected = value }
                is TextField        -> values?.get(it.id)?.let{ value -> it.text = value }
                is TextArea         -> values?.get(it.id)?.let{ value -> it.text = value }
                is ComboBox<*>      -> indices?.get(it.id)?.let{ value -> it.selectionModel.select(min(max(value,0),it.items.size-1)) }
                is ChoiceBox<*>     -> indices?.get(it.id)?.let{ value -> it.selectionModel.select(min(max(value,0),it.items.size-1)) }
                is CheckComboBox<*> -> lists?.get(it.id)?.let{ value ->
                    it.checkModel.clearChecks()
                    it.checkModel.checkedIndices.addAll(value)
                }
            }
            if( visibility ) {
                when(it) {
                    is Pane -> visibles?.get(it.id)?.let { value -> it.isVisible = value }
                    is Control -> {
                        visibles?.get(it.id)?.let { value ->
                            // ScrollBar.visible could not be set
                            try {
                                it.isVisible = value
                            } catch (e: Exception) {}
                        }
                        disables?.get(it.id)?.let { value -> it.isDisable = value }
                        if (it is TextInputControl) {
                            editables?.get(it.id)?.let { value -> it.isEditable = value }
                        }
                    }
                }
            }
        }

    }

    private fun getAllChildren(stage: Stage): List<EventTarget> {
        return stage.scene.root
            ?.allChildren
            ?.filter { it.fxId.isNotEmpty() }
            ?: emptyList()
    }

    private fun readWindowState(stage: Stage, currentInset: InsetProperty): WindowStateSnapshot {
        val previousNormalBoundary = stage.previousBoundary.boundary.copy()
        val hasPreviousNormalBoundary = previousNormalBoundary.width > 0 && previousNormalBoundary.height > 0
        val likelyMaximized =
            stage.isMaximized ||
            stage.previousBoundary.maximized ||
            (hasPreviousNormalBoundary && stage.fillsCurrentScreen(currentInset))
        return WindowStateSnapshot(
            maximized = likelyMaximized,
            hasNormalBoundary = hasPreviousNormalBoundary,
            normalBoundary = if(hasPreviousNormalBoundary) previousNormalBoundary else currentInset.copy(),
        )
    }

    private fun bindWindowState(stage: Stage) {
        if(!maximized) {
            inset?.bind(stage)
            stage.isMaximized = false
            return
        }

        resolveNormalBoundary()?.let { normalBoundary ->
            stage.previousBoundary = normalBoundary.toMaximizedProperty()
            stage.runWhenShown {
                restoreMaximized(stage, normalBoundary)
            }
        }
    }

    private fun resolveNormalBoundary(): InsetProperty? {
        return previousBoundary?.boundary
            ?.takeIf { it.width > 0 && it.height > 0 }
            ?.copy()
            ?: inset?.copy()
    }

    private fun Stage.runWhenShown(block: () -> Unit) {
        if(isShowing) {
            Platform.runLater(block)
            return
        }

        lateinit var handler: EventHandler<WindowEvent>
        handler = EventHandler {
            removeEventHandler(WindowEvent.WINDOW_SHOWN, handler)
            Platform.runLater(block)
        }
        addEventHandler(WindowEvent.WINDOW_SHOWN, handler)
    }

    private fun InsetProperty.toMaximizedProperty(): MaximizedProperty {
        return MaximizedProperty().apply {
            maximized = true
            boundary = copy()
        }
    }

    private fun restoreMaximized(stage: Stage, normalBoundary: InsetProperty, attempts: Int = 5, prime: Boolean = true) {
        if(prime) {
            stage.isMaximized = false
            normalBoundary.bind(stage)
            Platform.runLater {
                restoreMaximized(stage, normalBoundary, attempts, false)
            }
            return
        }

        stage.previousBoundary = normalBoundary.toMaximizedProperty()
        stage.isMaximized = true

        Platform.runLater {
            if(stage.fillsCurrentScreen()) {
                return@runLater
            }
            if(attempts <= 0) {
                return@runLater
            }

            stage.isMaximized = false
            normalBoundary.bind(stage)
            Platform.runLater {
                restoreMaximized(stage, normalBoundary, attempts - 1, false)
            }
        }
    }

    private fun skippable(node: EventTarget): Boolean {
        if( excludeKlass.isNotEmpty() && excludeKlass.any { it::class.isSuperclassOf(node::class) } ) return true
        if( includeKlass.isNotEmpty() && ! includeKlass.any { it::class.isSuperclassOf(node::class) } ) return true
        if( excludeId.isNotEmpty() && node.fxId in excludeId) return true
        if( includeId.isNotEmpty() && node.fxId !in includeId) return true
        if( excludeObject.isNotEmpty() && node in excludeObject) return true
        if( includeObject.isNotEmpty() && node !in includeObject) return true
        return false
    }

}
