package io.github.nayasis.kotlin.javafx.stage

import io.kotest.matchers.shouldBe
import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.layout.VBox
import javafx.stage.Stage
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.testfx.framework.junit5.ApplicationExtension
import org.testfx.framework.junit5.Start
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

@ExtendWith(ApplicationExtension::class)
class WatchMaximizedTest {

    private lateinit var stage: Stage

    @Start
    fun start(stage: Stage) {
        this.stage = stage
        stage.scene = Scene(VBox(), 640.0, 480.0)
        stage.show()
    }

    @Test
    fun `watchMaximized tracks latest normal boundary`() {
        runOnFxThread {
            stage.isMaximized = false
            stage.previousBoundary = MaximizedProperty()
            stage.watchMaximized()
            stage.x = 120.0
            stage.y = 140.0
            stage.width = 800.0
            stage.height = 600.0
        }
        waitForFxQueue()

        stage.previousBoundary.boundary.width shouldBe 800
        stage.previousBoundary.boundary.height shouldBe 600
    }

    @Test
    fun `watchMaximized keeps stored boundary when stage becomes maximized`() {
        runOnFxThread {
            stage.isMaximized = false
            stage.previousBoundary = MaximizedProperty()
            stage.watchMaximized()
            stage.x = 160.0
            stage.y = 180.0
            stage.width = 820.0
            stage.height = 620.0
        }
        waitForFxQueue()
        runOnFxThread {
            stage.isMaximized = true
        }
        waitForFxQueue()

        stage.previousBoundary.maximized shouldBe true
        stage.previousBoundary.boundary.width shouldBe 820
        stage.previousBoundary.boundary.height shouldBe 620
    }

    private fun waitForFxQueue() {
        runOnFxThread {}
    }

    private fun runOnFxThread(block: () -> Unit) {
        val latch = CountDownLatch(1)
        val error = AtomicReference<Throwable?>(null)

        Platform.runLater {
            try {
                block()
            } catch (e: Throwable) {
                error.set(e)
            } finally {
                latch.countDown()
            }
        }

        check(latch.await(5, TimeUnit.SECONDS)) { "Timed out waiting for JavaFX operation." }
        error.get()?.let { throw it }
    }

}
