@file:Suppress("MemberVisibilityCanBePrivate")

package io.github.nayasis.kotlin.javafx.stage.progress

import io.github.nayasis.kotlin.javafx.misc.runAsync
import io.github.nayasis.kotlin.javafx.misc.runAwait
import io.github.nayasis.kotlin.javafx.misc.runLater
import io.github.nayasis.kotlin.javafx.property.PositionProperty
import javafx.stage.Modality
import javafx.stage.Window

@Suppress("unused")
abstract class AbstractProgressDialog(progressCount: Int, title: String?) {

    val stage = ProgressDialogStage(progressCount)
    val size: Int
        get() = stage.progressBars.size

    protected var onSuccess: (() -> Unit)? = null
    protected var onFail: ((exception: Throwable) -> Unit)? = { e -> throw e }
    protected var onDone: (() -> Unit)? = null

    init {
        updateTitle(title)
    }

    fun initModality(modality: Modality) = stage.initModality(modality)
    fun initOwner(window: Window?) = stage.initOwner(window)
    fun updateTitle(title: String?) = stage.updateTitle(title)

    fun show() = stage.show()

    /**
     * Show dialog at specified point.
     *
     * @param point position to apply before showing. If null, shows at default position.
     */
    fun showAt(point: PositionProperty? = null) {
        point?.bind(stage)
        stage.show()
    }

    fun close() = runLater {
        stage.close()
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <T: AbstractProgressDialog> internalRunSync(point: PositionProperty? = null, task: ((dialog: T) -> Unit)?) {
        point?.bind(stage)
        stage.show()
        if(task == null) return
        val self = this as T
        runAwait { runTask(task,self) }
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <T: AbstractProgressDialog> internalRunAsync(point: PositionProperty? = null, task: ((dialog: T) -> Unit)?) {
        point?.bind(stage)
        stage.show()
        if(task == null) return
        val self = this as T
        runAsync {
            runTask(task,self)
        }
    }

    private fun <T: AbstractProgressDialog> runTask(task: ((dialog: T) -> Unit)?, dialog: T) {
        try {
            task?.invoke(dialog)
            runLater { stage.close() }
            onSuccess?.let {
                runLater { it.invoke() }
            }
        } catch (e: Throwable) {
            runLater { stage.close() }
            onFail?.let {
                runLater { it.invoke(e) }
            }
        } finally {
            onDone?.let {
                runLater { it.invoke() }
            }
        }
    }

}