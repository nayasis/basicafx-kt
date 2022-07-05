package com.github.nayasis.kotlin.javafx.control.basic

import tornadofx.App
import tornadofx.View
import tornadofx.hbox
import tornadofx.label
import tornadofx.launch
import tornadofx.textfield
import tornadofx.vbox

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
    }
}