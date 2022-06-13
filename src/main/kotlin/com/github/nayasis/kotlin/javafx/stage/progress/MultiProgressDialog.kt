package com.github.nayasis.kotlin.javafx.stage.progress

class MultiProgressDialog(progressCount: Int,title: String? = null): AbstractProgressDialog(progressCount,title) {

    fun updateProgress(index: Int, done: Number, max: Number) = stage.updateProgress(index,done,max)
    fun updateProgress(index: Int, percent: Number) = stage.updateProgress(index,percent)
    fun updateMessage(index: Int, message: String?) = stage.updateMessage(index,message)
    fun updateSubMessage(index: Int, message: String?) = stage.updateSubMessage(index,message)
    fun getProgress(index: Int): Double = stage.progressBars[index].progress

    fun runSync(task: ((dialog: MultiProgressDialog) -> Unit)?) = super.internalRunSync(task)
    fun runAsync(task: ((dialog: MultiProgressDialog) -> Unit)?) = super.internalRunAsync(task)

}