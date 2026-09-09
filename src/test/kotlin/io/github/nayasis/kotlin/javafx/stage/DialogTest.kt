package io.github.nayasis.kotlin.javafx.stage

import io.github.nayasis.kotlin.basica.core.number.floor
import io.github.nayasis.kotlin.javafx.app.FxView
import io.github.nayasis.kotlin.javafx.common.DemoApp
import io.github.nayasis.kotlin.javafx.misc.runAsync
import javafx.application.Application
import javafx.scene.control.Alert.AlertType.INFORMATION
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import java.lang.Thread.sleep

fun main(vararg args: String) {
    Application.launch(DialogTest::class.java, *args)
}

class DialogTest: DemoApp(DialogTestView())

private fun button(text: String, action: () -> Unit) = Button(text).apply { setOnAction { action() } }

private fun blueText(text: String, underline: Boolean = false) = Text(text).apply {
    style = "-fx-font-size: 16px; -fx-fill: blue;" + if(underline) " -fx-underline: true;" else ""
}

private fun decoratedText() = TextFlow(
    blueText("This is "),
    blueText("underlined ", underline = true),
    blueText("text"),
)

class DialogTestView: FxView("dialog test") {

    override val root = VBox().apply {

        children.add(Label("Test Label (1234567890)"))

        children.add(button("alert") {
            Dialog.alert("test alert (1234567890)")
        })

        children.add(button("alert (having contents)") {
            Dialog.alert("test alert (1234567890)", """
                Pellentesque 1234567890 habitant morbi tristique senectus et netus et malesuada fames ac
                turpis egestas. Vestibulum tortor quam, feugiat vitae, ultricies eget, tempor sit
                amet, ante. Donec eu libero sit amet quam egestas semper. Aenean ultricies mi
                vitae est. Mauris placerat eleifend leo. Quisque sit amet est et sapien
                ullamcorper pharetra. Vestibulum erat wisi, condimentum sed, commodo
                vitae, ornare sit amet, wisi. Aenean fermentum, elit eget tincidunt
                condimentum, eros ipsum rutrum orci, sagittis tempus lacus enim ac dui.
                Donec non enim in turpis pulvinar facilisis. Ut felis. Praesent dapibus, neque id
                cursus faucibus, tortor neque egestas augue, eu vulputate magna eros eu erat.
                Aliquam erat volutpat. Nam dui mi, tincidunt quis, accumsan porttitor, facilisis
                luctus, metus
            """.trimIndent())
        })

        children.add(button("alert decorated") {
            Dialog.dialog(INFORMATION).content(decoratedText()).showAndWait()
        })

        children.add(button("alert expanded decorated") {
            Dialog.dialog(INFORMATION).expand(decoratedText()).showAndWait()
        })

        children.add(button("error") {
            Dialog.error("test error (1234567890)", RuntimeException("test"))
        })

        children.add(button("confirm") {
            if( Dialog.confirm("is it ok (1234567890) ??") ) {
                Dialog.alert("it is ok !!")
            }
        })

        children.add(button("prompt") {
            // if 'esc' pressed, no error raised !
            val text = Dialog.prompt("input your parameter","merong 1234567890")
            Dialog.alert("[$text]")
        })

        children.add(button("alert various") {
            Dialog.dialog(INFORMATION).apply {
                title = "Title"
                headerText = "Header"
                contentText = "Content Text"
            }.showAndWait()
        })

        children.add(button("progress") {
            Dialog.progress("header") {
                val max = 40
                for (i in 1..max) {
                    it.updateProgress(i, max)
                    it.updateMessage("$i / $max")
                    it.updateTitle("title : $i")
                    it.updateSubMessage("${(it.getProgress()*100).toInt()}%")
                    sleep(100)
                }
            }
        })

        children.add(button("progress-multi") {
            Dialog.progressMulti(2,"header") {
                val max = 40
                for( idx in 0..1) {
                    for (i in 1..max) {
                        it.updateProgress(idx, i, max)
                        it.updateMessage(idx,"$i / $max")
                        it.updateSubMessage(idx,"${(it.getProgress(idx)*100).floor(1)}%")
                        sleep(100)
                    }
                }
            }
        })

        children.add(button("progress manually") {
            val progress = Dialog.progress("Terminal test")
            runAsync {
                val max = 40
                for (i in 1..max) {
                    println("$i / $max")
                    progress.updateMessage("$i / $max")
                    progress.updateProgress(i, max)
                    sleep(100)
                }
                progress.close()
            }
        })

        prefWidth = 300.0
        prefHeight = 200.0
        stylesheets.add("/css/common.css")

    }

}
