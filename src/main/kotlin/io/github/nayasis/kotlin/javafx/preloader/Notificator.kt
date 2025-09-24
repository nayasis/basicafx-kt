package io.github.nayasis.kotlin.javafx.preloader

import javafx.application.Preloader.PreloaderNotification
import java.lang.Double.isInfinite
import java.lang.Double.isNaN

class ProgressNotificator(
    val progress: Number? = null,
    val message: String? = null,
): PreloaderNotification {

    constructor(index: Number, max:Number, message: String? = null): this(progress(index.toDouble(), max.toDouble()), message)

}

private fun progress(index: Double, max: Double): Double? {
    return when {
        isInvalid(index) -> null
        isInvalid(max)   -> null
        else -> if(index > max) 1.0 else (index / max)
    }
}

private fun isInvalid(number: Double): Boolean
        = isInfinite(number) || isNaN(number) || number < 0

class ErrorNotificator(
    val message: String? = null,
    val throwable: Throwable? = null,
): PreloaderNotification

class CloseNotificator: PreloaderNotification

class HideNotificator: PreloaderNotification
