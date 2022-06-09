package com.github.nayasis.kotlin.javafx.stage.progress

class MultiProgressDialog(progressCount: Int,title: String? = null): AbstractProgressDialog(progressCount,title) {

    fun updateProgress(index: Int, done: Number, max: Number) = dialog.updateProgress(index,done,max)
    fun updateProgress(index: Int, percent: Number) = dialog.updateProgress(index,percent)
    fun updateMessage(index: Int, message: String?) = dialog.updateMessage(index,message)
    fun updateSubMessage(index: Int, message: String?) = dialog.updateSubMessage(index,message)
    fun getProgress(index: Int): Double = dialog.progressBars[index].progress

    fun runSync(task: ((dialog: MultiProgressDialog) -> Unit)?) = super.internalRunSync(task)
    fun runAsync(task: ((dialog: MultiProgressDialog) -> Unit)?) = super.internalRunAsync(task)

}