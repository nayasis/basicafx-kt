package com.github.nayasis.kotlin.javafx.control.combobox

import javafx.event.EventHandler
import mu.KotlinLogging
import tornadofx.App
import tornadofx.Stylesheet
import tornadofx.View
import tornadofx.action
import tornadofx.attachTo
import tornadofx.button
import tornadofx.launch
import tornadofx.vbox

private val logger = KotlinLogging.logger {}

fun main(args: Array<String>) {
    launch<ComboBoxTest>(args)
}

class ComboBoxTest: App(ComboBoxTestView::class,MyStylesheet::class)

private val list = ('A'..'Z').mapIndexed{ i, ch -> Item("$i", "$ch") }

class ComboBoxTestView: View("ComboBox test") {

    private val combo = ItemComboBox(list).apply {
        select = 0
        onAction = EventHandler { _ ->
            println( "onAction : $selectedItem" )
        }
    }

    override val root = vbox {
        combo.attachTo(this)
        button("progress manually") { action {
            combo.value = "3"
        }}
        prefWidth = 300.0
        prefHeight = 200.0
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
