package io.github.nayasis.kotlin.javafx.control.basic

import io.github.nayasis.kotlin.javafx.app.FxView
import io.github.nayasis.kotlin.javafx.common.DemoApp
import io.github.oshai.kotlinlogging.KotlinLogging
import javafx.application.Application
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox

private val logger = KotlinLogging.logger {}

fun main(vararg args: String) {
    Application.launch(TextFieldsTest::class.java, *args)
}

class TextFieldsTest: DemoApp(TextFieldsTestView())

class TextFieldsTestView: FxView("textfield test") {

    override val root = VBox(
        HBox(
            Label("mask"),
            TextField().apply { addMask("####-##-##") },
        ),
        HBox(
            Label("input context"),
            TextField().apply {
                setOnInputMethodTextChanged { e ->
                    logger.debug { "what ?" }
                    logger.debug { e }
                }
            },
        ),
    )

}
