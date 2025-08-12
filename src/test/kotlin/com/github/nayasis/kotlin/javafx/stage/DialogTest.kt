package com.github.nayasis.kotlin.javafx.stage

import com.github.nayasis.kotlin.basica.core.number.floor
import io.github.oshai.kotlinlogging.KotlinLogging
import javafx.scene.control.Alert.AlertType.INFORMATION
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import tornadofx.*
import java.lang.Thread.sleep

private val logger = KotlinLogging.logger {}

fun main(vararg args: String) {
    launch<DialogTest>(*args)
}

class DialogTest: App(DialogTestView::class)

class DialogTestView: View("dialog test") {

    override val root = vbox {
        label("Test Label (1234567890)")
        button("alert") {action {
            Dialog.alert("test alert (1234567890)")
        }}
        button("alert (having contents)") {action {
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
        }}
        button("alert decorated") { action {
            Dialog.dialog(INFORMATION).content(
                TextFlow(
                    Text("This is ").apply {
                        style = "-fx-font-size: 16px; -fx-fill: blue;"
                    },
                    Text("underlined ").apply {
                        style = "-fx-font-size: 16px; -fx-fill: blue; -fx-underline: true;"
                    },
                    Text("text").apply {
                        style = "-fx-font-size: 16px; -fx-fill: blue;"
                    },
                )
            ).showAndWait()
        }}
        button("alert expanded decorated") { action {
            Dialog.dialog(INFORMATION).expand(
                TextFlow(
                    Text("This is ").apply {
                        style = "-fx-font-size: 16px; -fx-fill: blue;"
                    },
                    Text("underlined ").apply {
                        style = "-fx-font-size: 16px; -fx-fill: blue; -fx-underline: true;"
                    },
                    Text("text").apply {
                        style = "-fx-font-size: 16px; -fx-fill: blue;"
                    },
                )
            ).showAndWait()
        }}
        button("error") {action {
            Dialog.error("test error (1234567890)", RuntimeException("test"))
        }}
        button("confirm") {action {
            if( Dialog.confirm("is it ok (1234567890) ??") ) {
                Dialog.alert("it is ok !!")
            }
        }}
        button("prompt") {action {
            // if 'esc' pressed, no error raised !
            val text = Dialog.prompt("input your parameter","merong 1234567890")
            Dialog.alert("[${text}]")
        }}
        button("alert various") {action {
            Dialog.dialog(INFORMATION).apply {
                title = "Title"
                headerText = "Header"
                contentText = "Content Text"
            }.showAndWait()
        }}
        button("progress") {action {
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
        }}
        button("progress-multi") {action {
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
        }}
        button("progress manually") { action {
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
        }}
        prefWidth = 300.0
        prefHeight = 200.0
        stylesheets.add("/css/common.css")
    }

}