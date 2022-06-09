package com.github.nayasis.kotlin.javafx.stage.progress

class ProgressDialog(title: String? = null): AbstractProgressDialog(1,title) {

    fun updateProgress(done: Number, max: Number) = dialog.updateProgress(0,done,max)
    fun updateProgress(percent: Number) = dialog.updateProgress(0,percent)
    fun updateMessage(message: String?) = dialog.updateMessage(0,message)
    fun updateSubMessage(message: String?) = dialog.updateSubMessage(0,message)
    fun getProgress(): Double = dialog.progressBars[0].progress

    fun runSync(task: ((dialog: ProgressDialog) -> Unit)?) = super.internalRunSync(task)
    fun runAsync(task: ((dialog: ProgressDialog) -> Unit)?) = super.internalRunAsync(task)

}