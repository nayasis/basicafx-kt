@file:Suppress("unused")

package io.github.nayasis.kotlin.javafx.property

import com.fasterxml.jackson.annotation.JsonIgnore
import io.github.nayasis.kotlin.javafx.control.basic.allChildren
import io.github.nayasis.kotlin.javafx.control.basic.fxId
import io.github.nayasis.kotlin.javafx.scene.previousZoomInset
import io.github.nayasis.kotlin.javafx.stage.BoundaryChecker
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
        val previousNormalBoundary = stage.previousBoundary.boundary.copy()
        val hasPreviousNormalBoundary = previousNormalBoundary.width > 0 && previousNormalBoundary.height > 0
        val likelyMaximized =
            stage.isMaximized ||
            stage.previousBoundary.maximized ||
            (hasPreviousNormalBoundary && isLikelyFullscreenBounds(stage, currentInset))
        val restoredBoundary = when {
            hasPreviousNormalBoundary -> previousNormalBoundary
            else -> currentInset.copy()
        }

        inset = currentInset
        maximized = likelyMaximized
        previousZoomInset = stage.scene.previousZoomInset
        previousBoundary = MaximizedProperty().apply {
            maximized = likelyMaximized && hasPreviousNormalBoundary
            boundary = restoredBoundary
        }

        if(!includeChildren) return

        getAllChildren(stage).forEach {
            if( skippable(it) ) return@forEach
            when(it) {
                is TableView<*> -> tables!![it.id] = TableProperty(it)
                is CheckMenuItem -> checks!![it.id] = it.isSelected
                is CheckBox -> checks!![it.id] = it.isSelected
                is TextField -> values!![it.id] = it.text ?: ""
                is TextArea -> values!![it.id] = it.text ?: ""
                is ComboBox<*> -> indices!![it.id] = it.selectionModel.selectedIndex
                is ChoiceBox<*> -> indices!![it.id] = it.selectionModel.selectedIndex
                is CheckComboBox<*> -> lists!![it.id] = it.checkModel.checkedIndices.toList()
            }
            when(it) {
                is Pane -> visibles!![it.id] = it.isVisible
                is Control -> {
                    visibles!![it.id] = it.isVisible
                    disables!![it.id] = it.isDisable
                    if (it is TextInputControl) {
                        editables!![it.id] = it.isEditable
                    }
                }
            }
        }

    }

    @Suppress("UNCHECKED_CAST")
    fun bind(stage: Stage?, visibility: Boolean = true, includeChildren: Boolean = true) {

        if( stage?.scene == null ) return

        if(maximized) {
            val boundary = previousBoundary?.boundary
                ?.takeIf { it.width > 0 && it.height > 0 }
                ?: inset

            boundary?.copy()?.let { normalBoundary ->
                stage.previousBoundary = MaximizedProperty().apply {
                    maximized = true
                    this.boundary = normalBoundary.copy()
                }

                if(stage.isShowing) {
                    Platform.runLater {
                        restoreMaximized(stage, normalBoundary)
                    }
                } else {
                    lateinit var handler: EventHandler<WindowEvent>
                    handler = EventHandler {
                        stage.removeEventHandler(WindowEvent.WINDOW_SHOWN, handler)
                        Platform.runLater {
                            restoreMaximized(stage, normalBoundary)
                        }
                    }
                    stage.addEventHandler(WindowEvent.WINDOW_SHOWN, handler)
                }
            }
        } else {
            inset?.bind(stage)
            stage.isMaximized = false
        }
        previousZoomInset?.let { stage.scene.previousZoomInset = it }

        if(!includeChildren) return

        getAllChildren(stage).forEach {
            if( skippable(it) ) return@forEach
            when(it) {
                is TableView<*> -> tables?.get(it.id)?.bind(it as TableView<Any>)
                is CheckMenuItem -> checks?.get(it.id)?.let{ value -> it.isSelected = value }
                is CheckBox -> checks?.get(it.id)?.let{ value -> it.isSelected = value }
                is TextField -> values?.get(it.id)?.let{ value -> it.text = value }
                is TextArea -> values?.get(it.id)?.let{ value -> it.text = value }
                is ComboBox<*> -> indices?.get(it.id)?.let{ value -> it.selectionModel.select(min(max(value,0),it.items.size-1)) }
                is ChoiceBox<*> -> indices?.get(it.id)?.let{ value -> it.selectionModel.select(min(max(value,0),it.items.size-1)) }
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

    private fun isLikelyFullscreenBounds(stage: Stage, inset: InsetProperty): Boolean {
        val screen = BoundaryChecker().getMajorScreen(stage).visualBounds
        val tolerance = 16
        return inset.width >= screen.width - tolerance &&
            inset.height >= screen.height - tolerance &&
            inset.x <= screen.minX + tolerance &&
            inset.y <= screen.minY + tolerance
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

        stage.previousBoundary = MaximizedProperty().apply {
            maximized = true
            boundary = normalBoundary.copy()
        }
        stage.isMaximized = true

        Platform.runLater {
            if(isLikelyFullscreenBounds(stage, InsetProperty(stage))) {
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
