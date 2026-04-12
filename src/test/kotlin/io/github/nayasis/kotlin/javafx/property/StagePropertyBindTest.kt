package io.github.nayasis.kotlin.javafx.property

import io.github.nayasis.kotlin.javafx.stage.MaximizedProperty
import io.github.nayasis.kotlin.javafx.stage.previousBoundary
import io.github.nayasis.kotlin.javafx.stage.watchMaximized
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
class StagePropertyBindTest {

    @Start
    fun start(stage: Stage) {
        stage.scene = Scene(VBox(), 320.0, 240.0)
        stage.show()
    }

    @Test
    fun `bind restores maximized stage after show`() {
        val restoredRef = AtomicReference<Stage>()

        runOnFxThread {
            val restored = Stage().apply {
                scene = Scene(VBox(), 700.0, 500.0)
                watchMaximized()
            }

            StageProperty(
                inset = InsetProperty(0, 0, 1920, 1080),
                maximized = true,
                previousBoundary = MaximizedProperty().apply {
                    maximized = true
                    boundary = InsetProperty(100, 120, 840, 620)
                },
            ).bind(restored, includeChildren = false)

            restored.show()
            restoredRef.set(restored)
        }

        waitForFxQueue()
        waitForFxQueue()

        restoredRef.get().isMaximized shouldBe true
        restoredRef.get().previousBoundary.boundary.width shouldBe 840
        restoredRef.get().previousBoundary.boundary.height shouldBe 620

        runOnFxThread {
            restoredRef.get().close()
        }
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
