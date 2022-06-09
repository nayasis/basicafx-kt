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

    val dialog = ProgressDialogStage(progressCount)
    var title: String?
        get() = dialog.titleHeader.text
        set(value) {
            updateTitle(value)
        }

    init {
        updateTitle(title)
    }

    fun initModality(modality: Modality) = dialog.initModality(modality)
    fun initOwner(window: Window?) = dialog.initOwner(window)
    fun updateTitle(title: String?) = dialog.updateTitle(title)

    fun show() = dialog.show()
    fun close() = runLater { dialog.close() }

    @Suppress("UNCHECKED_CAST")
    protected fun <T: AbstractProgressDialog> internalRunSync(task: ((dialog: T) -> Unit)?) {
        dialog.show()
        if(task == null) return
        val self = this
        val status = TaskStatus()
        runAsync(status) {
            task.invoke(self as T)
            runLater {
                dialog.close()
            }
        }
        status.completed.awaitUntil()
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <T: AbstractProgressDialog> internalRunAsync(task: ((dialog: T) -> Unit)?) {
        dialog.show()
        if(task == null) return
        val self = this
        runAsync {
            task.invoke(self as T)
            runLater {
                dialog.close()
            }
        }
    }

}