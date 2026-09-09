package io.github.nayasis.kotlin.javafx.app

import io.github.nayasis.kotlin.javafx.preloader.DefaultPreloader
import io.github.nayasis.kotlin.javafx.preloader.DefaultSplash
import javafx.application.Application
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.VBox

fun main() {
    DefaultPreloader.set(TestSplash::class)
    Application.launch(TestFxApp::class.java)
}

class TestSplash: DefaultSplash(527,297,"/image/test.png")

class TestFxApp: FxApp(TestFxView::class)

class TestFxView: FxView("test view") {

    override val root = VBox(
        Label("test label"),
        TextField(),
    )

    override fun onBeforeShow() {
        // verifies that a failure here is reported through the preloader
        throw RuntimeException("test error")
    }

}
