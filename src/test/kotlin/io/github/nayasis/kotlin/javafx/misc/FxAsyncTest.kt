package io.github.nayasis.kotlin.javafx.misc

import io.kotest.matchers.shouldBe
import javafx.embed.swing.JFXPanel
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

internal class FxAsyncTest {

    companion object {
        @BeforeAll
        @JvmStatic
        fun initJavaFx() {
            JFXPanel() // triggers JavaFX initialization
        }
    }

    @Test
    fun `runAsync runs the block on a pooled background thread`() {
        val caller = Thread.currentThread()
        val worker = AtomicReference<Thread?>()
        val latch = CountDownLatch(1)

        runAsync {
            worker.set(Thread.currentThread())
            latch.countDown()
        }

        latch.await(5, TimeUnit.SECONDS) shouldBe true
        (worker.get() === caller) shouldBe false
        worker.get()!!.name.startsWith("basicafx-async-") shouldBe true
    }

    @Test
    fun `runAwait runs the block on another thread and waits for it`() {
        val caller = Thread.currentThread()
        val worker = AtomicReference<Thread?>()
        val done = AtomicBoolean(false)

        runAwait {
            worker.set(Thread.currentThread())
            Thread.sleep(50)
            done.set(true)
        }

        done.get() shouldBe true
        (worker.get() === caller) shouldBe false
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
        val reported = CountDownLatch(1)
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            caught.set(e)
            reported.countDown()
        }
        try {
            var reachedNextStatement = false

            runAwait { throw IllegalStateException("boom") }
            reachedNextStatement = true

            reachedNextStatement shouldBe true
            reported.await(5, TimeUnit.SECONDS) shouldBe true
            caught.get()?.message shouldBe "boom"
        } finally {
            Thread.setDefaultUncaughtExceptionHandler(previous)
        }
    }

}
