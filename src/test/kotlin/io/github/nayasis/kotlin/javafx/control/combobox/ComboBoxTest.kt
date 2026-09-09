package io.github.nayasis.kotlin.javafx.control.combobox

import io.github.nayasis.kotlin.javafx.app.FxView
import io.github.nayasis.kotlin.javafx.common.DemoApp
import io.github.oshai.kotlinlogging.KotlinLogging
import javafx.application.Application
import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.layout.VBox

private val logger = KotlinLogging.logger {}

fun main(args: Array<String>) {
    Application.launch(ComboBoxTest::class.java, *args)
}

class ComboBoxTest: DemoApp(ComboBoxTestView(), "/basicafx/css/demo-font.css")

private val list = ('A'..'Z').mapIndexed{ _,ch -> ItemCombo("$ch") }

class ComboBoxTestView: FxView("ComboBox test") {

    private val combo = ItemComboBox(list).apply {
        select = 0
        onAction = EventHandler { _ ->
            println( "onAction : $selectedItem" )
        }
    }

    override val root = VBox(5.0).apply {

        children.add(combo)

        children.add(Button("select 'J'").apply { setOnAction {
            combo.value = "J"
        }})

        children.add(Button("remove C").apply { setOnAction {
            val rs = combo.removeItem("C")
            logger.debug { ">> remove 'C' : $rs" }
        }})

        children.add(Button("add C").apply { setOnAction {
            combo.addItem("C",index=2)
        }})

        children.add(Button("remove first").apply { setOnAction {
            combo.removeItem( combo.items.first() )
            combo.selectFirst()
        }})

        prefWidth = 300.0
        prefHeight = 200.0

    }

}
