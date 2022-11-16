package com.github.nayasis.kotlin.javafx.preloader

import javafx.application.Preloader as FxPreloader

abstract class NPreloader: FxPreloader() {

    override fun handleApplicationNotification(notificator: PreloaderNotification) {
        when(notificator) {
            is ProgressNotificator -> onProgress(notificator)
            is CloseNotificator -> onClose(notificator)
        }
    }

    abstract fun onProgress(notificator: ProgressNotificator)

    abstract fun onClose(notificator: CloseNotificator = CloseNotificator())

}