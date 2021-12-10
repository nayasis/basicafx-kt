package com.github.nayasis.kotlin.javafx.control.combobox

import javafx.application.Application
import javafx.event.EventHandler
import javafx.scene.control.ComboBox
import javafx.scene.text.Font
import javafx.util.StringConverter
import mu.KotlinLogging
import tornadofx.App
import tornadofx.Stylesheet
import tornadofx.View
import tornadofx.action
import tornadofx.button
import tornadofx.combobox
import tornadofx.launch
import tornadofx.vbox

private val logger = KotlinLogging.logger {}

fun main(args: Array<String>) {
    launch<ComboBoxTest>(args)
}

class ComboBoxTest: App(ComboBoxTestView::class,MyStylesheet::class)

private val list = listOf(Item("A","1"),Item("B","2"),Item("C","3"),Item("D","4"),Item("E","5"),Item("F","6"))
    .map { it.value to it.label }.toMap()

class ComboBoxTestView: View("ComboBox test") {

    lateinit var combo: ComboBox<String>

    override val root = vbox {
        combo = combobox {
            converter = object: StringConverter<String>() {
                override fun toString(key: String?): String {
                    return list[key] ?: ""
                }
                override fun fromString(key: String): String? {
                    println( ">> from : ${key}")
                    return list[key]
                }
            }
            items.addAll(list.keys)
//            valueProperty().onChange { item ->
//                println( "changed : ${item}" )
//            }
//            addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED) { event ->
//
//            }
            selectionModel.select(0)
            onAction = EventHandler { event ->
                println( "onAction : $value" )
            }

        }

        button("progress manually") { action {
            combo.value = "3"
        }}
        prefWidth = 300.0
        prefHeight = 200.0
    }

    init {
        val userAgentStylesheet = Application.getUserAgentStylesheet()

        logger.debug {
            "style sheet : ${userAgentStylesheet}"
            "font : ${Font.getDefault().name}"
        }

    }

}

// set default font
class MyStylesheet : Stylesheet() {
    init {
        root {
            fontFamily = "Arial"
        }
    }
}
