package io.github.nayasis.kotlin.javafx.misc

import io.github.nayasis.kotlin.basica.etc.error
import io.github.oshai.kotlinlogging.KotlinLogging
import javafx.application.Platform
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread
import kotlin.time.Duration

private val logger = KotlinLogging.logger {}

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

/**
 * run the specified function on another thread and wait for termination.
 *
 * when called from the JavaFx thread, a nested event loop keeps the UI
 * responsive while waiting, so this halts further execution without
 * blocking rendering.
 *
 * a failure inside [func] is not propagated to the caller. it is handed to
 * the default uncaught exception handler on the JavaFx thread, so the caller
 * keeps running.
 *
 * @param func function
 */
fun runAwait(func: () -> Unit) {

    if(!Platform.isFxApplicationThread()) {
        // a nested event loop is only allowed on the JavaFx thread
        val latch = CountDownLatch(1)
        startAwaitTask(func) { latch.countDown() }
        latch.await()
        return
    }

    check(Platform.canStartNestedEventLoop()) { "runAwait is not allowed during animation or layout processing" }

    val key = Any()
    // the exit below is posted with runLater, so it can only run after the
    // nested event loop is entered even when [func] finishes immediately.
    startAwaitTask(func) { Platform.runLater { Platform.exitNestedEventLoop(key, null) } }
    Platform.enterNestedEventLoop(key)
}

private fun startAwaitTask(func: () -> Unit, onDone: () -> Unit) {
    thread(name = "basicafx-await", isDaemon = false) {
        try {
            func()
        } catch (e: Throwable) {
            reportUncaught(e)
        } finally {
            onDone()
        }
    }
}

/**
 * hand [e] to the default uncaught exception handler on the JavaFx thread.
 * the handler decides whether to show it to the user.
 */
private fun reportUncaught(e: Throwable) {
    val handler = Thread.getDefaultUncaughtExceptionHandler()
    if(handler == null) {
        logger.error(e)
        return
    }
    runCatching {
        Platform.runLater { handler.uncaughtException(Thread.currentThread(), e) }
    }.onFailure {
        // JavaFx is not running (e.g. plain unit test): report on this thread
        handler.uncaughtException(Thread.currentThread(), e)
    }
}
