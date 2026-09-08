package io.github.nayasis.kotlin.javafx.misc

import io.github.nayasis.kotlin.basica.etc.error
import io.github.oshai.kotlinlogging.KotlinLogging
import javafx.application.Platform
import javafx.concurrent.Task
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

private val logger = KotlinLogging.logger {}

private val pools = HashMap<Boolean, ExecutorService>()

private class AsyncThreadFactory(private val daemon: Boolean): ThreadFactory {
    private val counter = AtomicLong(0L)
    override fun newThread(runnable: Runnable): Thread =
        Thread(runnable, "basicafx-async-${counter.incrementAndGet()}" + if(daemon) "-daemon" else "").apply {
            isDaemon = daemon
        }
}

private fun pool(daemon: Boolean): ExecutorService = synchronized(pools) {
    pools.getOrPut(daemon) { Executors.newCachedThreadPool(AsyncThreadFactory(daemon)) }
}

/**
 * run [func] on a background thread.
 *
 * threads are reused from a cached pool, so short tasks do not pay for
 * thread creation.
 *
 * a failure inside [func] is not propagated to the caller. it is handed to
 * the default uncaught exception handler on the JavaFx thread, so the caller
 * keeps running and the handler decides how to surface it.
 *
 * @param daemon run on a daemon thread. daemon tasks do not keep the JVM alive.
 * @param func function
 * @return the running task, to observe or cancel it
 */
fun <T> runAsync(daemon: Boolean = false, func: () -> T): Task<T> {
    val task = object: Task<T>() {
        override fun call(): T = func()
    }
    // setOnFailed is fired on the JavaFx thread, which is what the default
    // handler needs to be able to show the failure to the user.
    task.setOnFailed { reportUncaught(task.exception) }
    pool(daemon).execute(task)
    return task
}

/**
 * run [func] on a background thread and wait for termination.
 *
 * when called from the JavaFx thread, a nested event loop keeps the UI
 * responsive while waiting, so this halts further execution without blocking
 * rendering. failures are routed as described in [runAsync].
 *
 * @param func function
 */
fun runAwait(func: () -> Unit) {

    if(!Platform.isFxApplicationThread()) {
        // a nested event loop is only allowed on the JavaFx thread
        val latch = CountDownLatch(1)
        runAsync { try { func() } finally { latch.countDown() } }
        latch.await()
        return
    }

    check(Platform.canStartNestedEventLoop()) { "runAwait is not allowed during animation or layout processing" }

    val key = Any()
    // the exit is posted with runLater, so it can only run once the nested
    // event loop below is entered, even when [func] finishes immediately.
    runAsync {
        try { func() } finally { Platform.runLater { Platform.exitNestedEventLoop(key, null) } }
    }
    Platform.enterNestedEventLoop(key)
}

/**
 * shut down the background pools and wait up to [timeoutMillis] for running
 * tasks to finish. non-daemon tasks keep the JVM alive until this is called.
 */
fun shutdownAsyncPools(timeoutMillis: Long = 1_000) = synchronized(pools) {
    pools.values.forEach { it.awaitTermination(timeoutMillis) }
    pools.clear()
}

private fun ExecutorService.awaitTermination(timeoutMillis: Long) {
    shutdown()
    try {
        if(!awaitTermination(timeoutMillis, TimeUnit.MILLISECONDS)) {
            shutdownNow()
            if(!awaitTermination(timeoutMillis, TimeUnit.MILLISECONDS))
                logger.warn { "async pool did not terminate" }
        }
    } catch (e: InterruptedException) {
        shutdownNow()
        Thread.currentThread().interrupt()
    }
}

/**
 * hand [e] to the default uncaught exception handler on the JavaFx thread.
 */
private fun reportUncaught(e: Throwable?) {
    if(e == null) return
    val handler = Thread.getDefaultUncaughtExceptionHandler()
    if(handler == null) {
        logger.error(e)
        return
    }
    handler.uncaughtException(Thread.currentThread(), e)
}
