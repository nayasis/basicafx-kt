@file:Suppress("MemberVisibilityCanBePrivate")

package com.github.nayasis.kotlin.javafx.stage.progress

import javafx.stage.Modality
import javafx.stage.Window
import mu.KotlinLogging
import tornadofx.TaskStatus
import tornadofx.awaitUntil
import tornadofx.runAsync
import tornadofx.runLater

private val logger = KotlinLogging.logger {}

abstract class AbstractProgressDialog(progressCount: Int, title: String?) {

    val stage = ProgressDialogStage(progressCount)

    init {
        updateTitle(title)
    }

    fun initModality(modality: Modality) = stage.initModality(modality)
    fun initOwner(window: Window?) = stage.initOwner(window)
    fun updateTitle(title: String?) = stage.updateTitle(title)

    fun show() = stage.show()
    fun close() = runLater { stage.close() }

    @Suppress("UNCHECKED_CAST")
    protected fun <T: AbstractProgressDialog> internalRunSync(task: ((dialog: T) -> Unit)?) {
        stage.show()
        if(task == null) return
        val self = this
        val status = TaskStatus()
        runAsync(status) {
            task.invoke(self as T)
            runLater {
                stage.close()
            }
        }
        status.completed.awaitUntil()
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <T: AbstractProgressDialog> internalRunAsync(task: ((dialog: T) -> Unit)?) {
        stage.show()
        if(task == null) return
        val self = this
        runAsync {
            task.invoke(self as T)
            runLater {
                stage.close()
            }
        }
    }

}