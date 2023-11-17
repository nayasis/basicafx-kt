package com.github.nayasis.kotlin.javafx.preloader

import com.github.nayasis.kotlin.javafx.stage.Dialog
import com.sun.javafx.application.LauncherImpl
import javafx.application.Preloader
import javafx.stage.Stage
import kotlinx.coroutines.runBlocking
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmName

abstract class BasePreloader: Preloader() {

    final override fun handleApplicationNotification(notificator: PreloaderNotification) {
        when(notificator) {
            is ProgressNotificator -> onProgress(notificator)
            is ErrorNotificator -> onError(notificator.message, notificator.throwable)
            is CloseNotificator -> onClose()
            is HideNotificator -> onHide()
        }
    }

    final override fun start(primaryStage: Stage) {
        stage = primaryStage
        onStart(stage!!)
    }

    abstract fun onStart(stage: Stage)

    abstract fun onProgress(notificator: ProgressNotificator)

    open fun onClose() {
        stage?.close()
        stage = null
    }

    open fun onError(message: String?, throwable: Throwable?) {
        stage?.hide()
        Dialog.error(message, throwable, true)
        stage?.close()
    }

    open fun onHide() {}

    companion object {

        private var stage: Stage? = null

        fun isShowing(): Boolean {
            return stage?.isShowing ?: false
        }

        fun set(preloader: KClass<out BasePreloader>) {
            System.setProperty("javafx.preloader", preloader.jvmName)
            System.setProperty("java.awt.headless", "false")
        }

        private fun notify(notificator: PreloaderNotification) {
            LauncherImpl.notifyPreloader(null,notificator)
        }

        fun notifyProgress(percent: Number, message: String? = null) {
            notify(ProgressNotificator(percent.toDouble(),message))
        }

        fun notifyProgress(index: Number, max: Number, message: String? = null) {
            notify(ProgressNotificator(index,max,message))
        }

        fun notifyError(message: String? = null, throwable: Throwable? = null) {
            notify(ErrorNotificator(message,throwable))
        }

        fun close() = runBlocking {
            notify(CloseNotificator())
        }

    }

}