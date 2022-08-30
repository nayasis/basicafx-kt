package com.github.nayasis.kotlin.javafx.control.basic

import mu.KotlinLogging
import tornadofx.App
import tornadofx.View
import tornadofx.hbox
import tornadofx.label
import tornadofx.launch
import tornadofx.onChange
import tornadofx.textfield
import tornadofx.vbox

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