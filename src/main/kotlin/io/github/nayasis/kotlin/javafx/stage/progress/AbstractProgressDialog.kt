@file:Suppress("MemberVisibilityCanBePrivate")

package io.github.nayasis.kotlin.javafx.stage.progress

import io.github.nayasis.kotlin.javafx.property.InsetProperty
import javafx.stage.Modality
import javafx.stage.Window
import tornadofx.TaskStatus
import tornadofx.awaitUntil
import tornadofx.runAsync
import tornadofx.runLater

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
     * Show dialog at specified bounds.
     *
     * @param bounds position and size to apply before showing. If null, shows at default position.
     */
    fun showAt(bounds: InsetProperty? = null) {
        bounds?.bind(stage)
        stage.show()
    }

    fun close() = runLater {
        stage.close()
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <T: AbstractProgressDialog> internalRunSync(bounds: InsetProperty? = null, task: ((dialog: T) -> Unit)?) {
        bounds?.bind(stage)
        stage.show()
        if(task == null) return
        val self = this as T
        val status = TaskStatus()
        runAsync(status) {
            runTask(task,self)
        }
        status.completed.awaitUntil()
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <T: AbstractProgressDialog> internalRunAsync(bounds: InsetProperty? = null, task: ((dialog: T) -> Unit)?) {
        bounds?.bind(stage)
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