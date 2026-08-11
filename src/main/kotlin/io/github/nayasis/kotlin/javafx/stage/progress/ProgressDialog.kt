package io.github.nayasis.kotlin.javafx.stage.progress

import io.github.nayasis.kotlin.javafx.property.PositionProperty
import javafx.scene.Node

class ProgressDialog(title: String? = null): AbstractProgressDialog(1,title) {

    fun updateProgress(done: Number, max: Number) = stage.updateProgress(0,done,max)
    fun updateProgress(percent: Number) = stage.updateProgress(0,percent)
    fun updateMessage(message: String?) = stage.updateMessage(0,message)
    fun updateSubMessage(message: String?) = stage.updateSubMessage(0,message)
    fun updateSubMessageAsProgress(format: String = "%.1f%%") = stage.updateSubMessageAsProgress(0,format)
    fun getProgress(): Double = stage.progressBars[0].progress
    fun addHeaderRight(node: Node): ProgressDialog {
        stage.addHeaderRight(node)
        return this
    }

    fun runSync(task: ((dialog: ProgressDialog) -> Unit)?) {
        super.internalRunSync(null, task)
    }

    fun runSync(point: PositionProperty?, task: ((dialog: ProgressDialog) -> Unit)?) {
        super.internalRunSync(point, task)
    }

    fun runAsync(task: ((dialog: ProgressDialog) -> Unit)?): ProgressDialog {
        super.internalRunAsync(null, task)
        return this
    }

    fun runAsync(point: PositionProperty?, task: ((dialog: ProgressDialog) -> Unit)?): ProgressDialog {
        super.internalRunAsync(point, task)
        return this
    }

    fun run(async: Boolean, task: ((dialog: ProgressDialog) -> Unit)?) {
        if(async) {
            super.internalRunAsync(null, task)
        } else {
            super.internalRunSync(null, task)
        }
    }

    fun run(async: Boolean, point: PositionProperty?, task: ((dialog: ProgressDialog) -> Unit)?) {
        if(async) {
            super.internalRunAsync(point, task)
        } else {
            super.internalRunSync(point, task)
        }
    }

    fun setOnSuccess(callback: (() -> Unit)): ProgressDialog {
        super.onSuccess = callback
        return this
    }

    fun setOnFail(callback: ((exception: Throwable) -> Unit)): ProgressDialog {
        super.onFail = callback
        return this
    }

    fun setOnDone(callback: (() -> Unit)): ProgressDialog {
        super.onDone = callback
        return this
    }

}
