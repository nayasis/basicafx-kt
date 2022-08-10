package com.github.nayasis.kotlin.javafx.common

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.testfx.api.FxToolkit
import org.testfx.framework.junit5.ApplicationTest
import org.testfx.util.WaitForAsyncUtils
import java.util.function.Supplier


open class TestFxJunitAppRunner: ApplicationTest() {

    @BeforeEach
    fun runAppToTests() {
        FxToolkit.registerPrimaryStage()
        FxToolkit.setupApplication(Supplier<Application> { Main() })
        FxToolkit.showStage()
        WaitForAsyncUtils.waitForFxEvents(100)
    }

    @AfterEach
    fun stopApp() {
        FxToolkit.cleanupStages()
    }

    override fun start(primaryStage: Stage) {
        primaryStage.toFront()
    }

}

fun main(args: Array<String>) {
    Application.launch(*args)
}

class Main: Application() {
    override fun start(primaryStage: Stage) {
        primaryStage.title = "Hello World!"
        val button = Button("click me!")
        button.setOnAction { actionEvent -> button.setText("clicked!") }
        val root = StackPane()
        root.children.add(button)
        primaryStage.scene = Scene(root, 300.0, 250.0)
        primaryStage.show()
    }
//    companion object {
//        @JvmStatic
//        fun main(args: Array<String>) {
//            launch(*args)
//        }
//    }
}