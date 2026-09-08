package io.github.nayasis.kotlin.javafx.misc

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

internal class FxThreadsTest {

    @Test
    fun `runAwait runs the block on another thread and waits for it`() {
        val caller = Thread.currentThread()
        var worker: Thread? = null
        val done = AtomicBoolean(false)

        runAwait {
            worker = Thread.currentThread()
            Thread.sleep(50)
            done.set(true)
        }

        done.get() shouldBe true
        (worker === caller) shouldBe false
    }

    /**
     * tornadofx routed task failures to the default uncaught exception handler
     * instead of the call site, and callers rely on that: a failing load must
     * not abort the caller's remaining initialization.
     */
    @Test
    fun `runAwait hands a failure to the default handler instead of the caller`() {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        val caught = AtomicReference<Throwable?>()
        Thread.setDefaultUncaughtExceptionHandler { _, e -> caught.set(e) }
        try {
            var reachedNextStatement = false

            runAwait { throw IllegalStateException("boom") }
            reachedNextStatement = true

            reachedNextStatement shouldBe true
            caught.get()?.message shouldBe "boom"
        } finally {
            Thread.setDefaultUncaughtExceptionHandler(previous)
        }
    }

}
