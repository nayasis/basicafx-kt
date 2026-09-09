package io.github.nayasis.kotlin.javafx.app

import io.kotest.matchers.shouldBe
import javafx.embed.swing.JFXPanel
import javafx.scene.control.Label
import javafx.scene.layout.AnchorPane
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

private class TestView: FxView("test title") {
    override val root: AnchorPane by fxml("/view/fxview-test.fxml")
    val labelHello: Label by fxid()
    val labelMissing: Label by fxid()
}

internal class FxViewTest {

    companion object {
        @BeforeAll
        @JvmStatic
        fun initJavaFx() {
            JFXPanel() // triggers JavaFX initialization
        }
    }

    @Test
    fun `fxid triggers the fxml load even when root is untouched`() {
        val view = TestView()

        view.labelHello.text shouldBe "hello"
        view.title shouldBe "test title"
    }

    @Test
    fun `root loads the fxml declared by the delegate`() {
        val view = TestView()

        view.root.children.size shouldBe 1
    }

    @Test
    fun `an unresolved fxid fails loudly instead of returning null`() {
        val view = TestView()

        assertThrows<IllegalArgumentException> { view.labelMissing }
    }

    @Test
    fun `currentStage is null while the view is not on a stage`() {
        val view = TestView()

        view.currentStage shouldBe null
    }

}
