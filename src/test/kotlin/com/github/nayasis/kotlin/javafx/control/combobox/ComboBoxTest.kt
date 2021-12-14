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

private val list = ('A'..'Z').mapIndexed{ i, ch -> Item("$ch") }

class ComboBoxTestView: View("ComboBox test") {

    private val combo = ItemComboBox(list).apply {
        select = 0
        onAction = EventHandler { _ ->
            println( "onAction : $selectedItem" )
        }
    }

    override val root = vbox(spacing = 5) {

        combo.attachTo(this)

        button("select 'J'") { action {
            combo.value = "J"
        }}

        button("remove C") { action {
            val rs = combo.removeItem("C")
            logger.debug { ">> remove 'C' : ${rs}" }
        }}

        button("add C") { action {
            combo.setItem("C",index=2)
        }}

        button("remove first") { action {
            combo.removeItem( combo.items.first() )
            combo.selectFirst()
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
