package com.github.nayasis.kotlin.javafx.control.basic

import io.github.oshai.kotlinlogging.KotlinLogging
import tornadofx.*

private val logger = KotlinLogging.logger {}

fun main(vararg args: String) {
    launch<TextFieldsTest>(*args)
}

class TextFieldsTest: App(TextFieldsTestView::class)

class TextFieldsTestView: View("textfield test") {
    override val root = vbox {
        hbox {
            label("mask")
            textfield {
                addMask("####-##-##")
            }
        }
        hbox {
            label("input context")
            textfield {
                setOnInputMethodTextChanged { e ->
                    logger.debug { "what ?" }
                    logger.debug { e }
                }
            }
        }
    }
}