package io.github.nayasis.kotlin.javafx.stage.progress

import io.github.nayasis.kotlin.javafx.property.InsetProperty
import javafx.scene.Node

class MultiProgressDialog(progressCount: Int,title: String? = null): AbstractProgressDialog(progressCount,title) {

    fun updateProgress(index: Int, done: Number, max: Number) = stage.updateProgress(index,done,max)
    fun updateProgress(index: Int, percent: Number) = stage.updateProgress(index,percent)
    fun updateMessage(index: Int, message: String?) = stage.updateMessage(index,message)
    fun updateSubMessage(index: Int, message: String?) = stage.updateSubMessage(index,message)
    fun updateSubMessageAsProgress(index: Int, format: String = "%.1f%%") = stage.updateSubMessageAsProgress(index,format)
    fun getProgress(index: Int): Double = stage.progressBars[index].progress
    fun addHeaderRight(node: Node): MultiProgressDialog {
        stage.addHeaderRight(node)
        return this
    }

    fun runSync(task: ((dialog: MultiProgressDialog) -> Unit)?) {
        super.internalRunSync(null, task)
    }

    fun runSync(bounds: InsetProperty?, task: ((dialog: MultiProgressDialog) -> Unit)?) {
        super.internalRunSync(bounds, task)
    }

    fun runAsync(task: ((dialog: MultiProgressDialog) -> Unit)?): MultiProgressDialog {
        super.internalRunAsync(null, task)
        return this
    }

    fun runAsync(bounds: InsetProperty?, task: ((dialog: MultiProgressDialog) -> Unit)?): MultiProgressDialog {
        super.internalRunAsync(bounds, task)
        return this
    }

    fun run(async: Boolean, task: ((dialog: MultiProgressDialog) -> Unit)?) {
        if(async) {
            super.internalRunAsync(null, task)
        } else {
            super.internalRunSync(null, task)
        }
    }

    fun run(async: Boolean, bounds: InsetProperty?, task: ((dialog: MultiProgressDialog) -> Unit)?) {
        if(async) {
            super.internalRunAsync(bounds, task)
        } else {
            super.internalRunSync(bounds, task)
        }
    }

    fun setOnSuccess(callback: (() -> Unit)): MultiProgressDialog {
        super.onSuccess = callback
        return this
    }

    fun setOnFail(callback: ((exception: Throwable) -> Unit)): MultiProgressDialog {
        super.onFail = callback
        return this
    }

    fun setOnDone(callback: (() -> Unit)): MultiProgressDialog {
        super.onDone = callback
        return this
    }

}
