package com.github.nayasis.kotlin.javafx.stage

import com.github.nayasis.kotlin.basica.core.number.floor
import javafx.application.Application
import javafx.scene.text.Font
import mu.KotlinLogging
import tornadofx.App
import tornadofx.Stylesheet
import tornadofx.View
import tornadofx.action
import tornadofx.button
import tornadofx.launch
import tornadofx.vbox
import java.lang.Thread.sleep

private val logger = KotlinLogging.logger {}

fun main(vararg args: String) {
    launch<DialogTest>(*args)
}

class DialogTest: App(DialogTestView::class,MyStylesheet::class)

class DialogTestView: View("dialog test") {

    override val root = vbox {
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
            val text = Dialog.prompt("input your parameter","merong")
            Dialog.alert("[${text}]")
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
    }

    init {
        val userAgentStylesheet = Application.getUserAgentStylesheet()
        logger.trace {"""
            style sheet : $userAgentStylesheet
            font        : ${Font.getDefault().name}
        """.trimIndent()}
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