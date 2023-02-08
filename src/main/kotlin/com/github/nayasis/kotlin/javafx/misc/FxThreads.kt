package com.github.nayasis.kotlin.javafx.misc

import javafx.application.Platform
import tornadofx.FXTask
import tornadofx.TaskStatus
import tornadofx.awaitUntil
import tornadofx.runAsync
import java.util.concurrent.CountDownLatch

/**
 * run the specified function on JavaFx thread in future and wait for termination.
 * @param func function
 */
fun <T> runSync(func: FXTask<*>.() -> T) {
    val status = TaskStatus()
    runAsync(status, func)
    status.completed.awaitUntil()
}

fun runAndWait(func: () -> Unit) {
    if(Platform.isFxApplicationThread()) {
        func.invoke()
    } else {
        val doneLatch = CountDownLatch(1)
        Platform.runLater {
            try {
                func.invoke()
            } finally {
                doneLatch.countDown()
            }
        }
        try {
            doneLatch.await()
        } catch (ex: InterruptedException) {
            ex.printStackTrace()
        }
    }
}