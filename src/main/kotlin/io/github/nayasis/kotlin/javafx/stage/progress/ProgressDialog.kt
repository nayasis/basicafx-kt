package io.github.nayasis.kotlin.javafx.stage.progress

class ProgressDialog(title: String? = null): AbstractProgressDialog(1,title) {

    fun updateProgress(done: Number, max: Number) = stage.updateProgress(0,done,max)
    fun updateProgress(percent: Number) = stage.updateProgress(0,percent)
    fun updateMessage(message: String?) = stage.updateMessage(0,message)
    fun updateSubMessage(message: String?) = stage.updateSubMessage(0,message)
    fun updateSubMessageAsProgress(format: String = "%.1f%%") = stage.updateSubMessageAsProgress(0,format)
    fun getProgress(): Double = stage.progressBars[0].progress

    fun runSync(task: ((dialog: ProgressDialog) -> Unit)?) {
        super.internalRunSync(task)
    }

    fun runAsync(task: ((dialog: ProgressDialog) -> Unit)?): ProgressDialog {
        super.internalRunAsync(task)
        return this
    }

    fun run(async: Boolean, task: ((dialog: ProgressDialog) -> Unit)?) {
        if(async) {
            super.internalRunAsync(task)
        } else {
            super.internalRunSync(task)
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