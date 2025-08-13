package io.github.nayasis.kotlin.javafx.preloader

import javafx.application.Preloader.PreloaderNotification
import java.lang.Double.isInfinite
import java.lang.Double.isNaN

class ProgressNotificator(
    var progress: Double = 0.0,
    var message: String? = null,
): PreloaderNotification {

    constructor(index: Number, max:Number, message: String? = null): this(0.0, message) {
        progress(index,max)
    }

    fun progress(index: Number, max: Number) {
        progress = progress(index.toDouble(), max.toDouble())
    }

    private fun progress(index: Double, max: Double): Double {
        return when {
            isInvalid(index) -> 0.0
            isInvalid(max) -> 0.0
            else -> {
                if(index > max) 1.0 else (index / max)
            }
        }
    }

    private fun isInvalid(number: Double): Boolean
        = isInfinite(number) || isNaN(number) || number < 0

}

class ErrorNotificator(
    var message: String? = null,
    var throwable: Throwable? = null,
): PreloaderNotification

class CloseNotificator: PreloaderNotification

class HideNotificator: PreloaderNotification
