package io.github.nayasis.kotlin.javafx.misc

import tornadofx.FXTask
import tornadofx.TaskStatus
import tornadofx.awaitUntil
import tornadofx.runAsync

/**
 * run the specified function on JavaFx thread in future and wait for termination.
 * @param func function
 */
fun <T> runSync(func: FXTask<*>.() -> T) {
    val status = TaskStatus()
    runAsync(status, func)
    status.completed.awaitUntil()
}