package io.github.nayasis.kotlin.javafx.preloader

import io.github.nayasis.kotlin.javafx.app.FxApp
import io.github.nayasis.kotlin.javafx.app.FxView
import io.github.nayasis.kotlin.javafx.misc.runAwait
import javafx.application.Application
import javafx.beans.property.SimpleIntegerProperty
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import java.lang.Thread.sleep

fun main(args: Array<String>) {
    DefaultPreloader.set(TestSplash::class)
    Application.launch(DefaultPreloaderTest::class.java, *args)
}

class DefaultPreloaderTest: FxApp(MainView::class)

class MainView: FxView("Hello world!") {

    var count = SimpleIntegerProperty(0)

    override val root = VBox(
        Button("click").apply {
            setOnAction { count.set(count.get() + 1) }
        },
        Label().apply {
            textProperty().bind(count.asString())
        },
    )

    override fun onBeforeShow() {
        runAwait {
            sleep(1000)
        }
        DefaultPreloader.close()
    }

}

class TestSplash: DefaultSplash(527,297,"/splash/splash.jpg")
