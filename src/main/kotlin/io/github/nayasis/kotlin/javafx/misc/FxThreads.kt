package io.github.nayasis.kotlin.javafx.misc

import javafx.application.Platform
import java.util.Timer
import java.util.TimerTask
import kotlin.time.Duration

private val delayTimer: Timer by lazy { Timer("basicafx-runLater", true) }

/**
 * run the specified function on JavaFx thread.
 * @param func function
 */
fun runLater(func: () -> Unit) = Platform.runLater(func)

/**
 * run the specified function on JavaFx thread after [delay].
 *
 * can be called from any thread. cancel the returned task to abort the
 * pending execution.
 *
 * @param delay delay before execution
 * @param func function
 * @return the scheduled task
 */
fun runLater(delay: Duration, func: () -> Unit): TimerTask {
    val task = object: TimerTask() {
        override fun run() = Platform.runLater(func)
    }
    delayTimer.schedule(task, delay.inWholeMilliseconds)
    return task
}
