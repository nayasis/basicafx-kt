package io.github.nayasis.kotlin.javafx.stage.progress

import io.github.nayasis.kotlin.javafx.stage.Dialog
import javafx.stage.Stage
import tornadofx.App
import tornadofx.Stylesheet.Companion.button
import tornadofx.View
import tornadofx.action
import tornadofx.button
import tornadofx.launch
import tornadofx.runLater
import tornadofx.vbox
import java.lang.Thread.sleep
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    launch<ProgressDialogTest>(args)
}

class ProgressDialogTest: App(ProgressDialogView::class)

class ProgressDialogView: View("progress dialog test") {
    override val root = vbox {
        button("sync") { action {
            ProgressDialog("header")
//                .setOnFail { e -> Dialog.error(e) }
                .setOnDone {
                    Dialog.alert("work done - sync")
                }
                .runSync {
                    val max = 40
                    for (i in 1..max) {
                        println( "$i to $max" )
                        it.updateProgress(i, max)
                        it.updateMessage("$i / $max")
                        sleep(100)
                        if( i == 20 ) {
                            throw RuntimeException("Exception test")
                        }
                    }
                }
        }}
        button("async") { action {
            ProgressDialog("header")
                .runAsync {
                    val max = 40
                    for (i in 1..max) {
                        println( "$i to $max" )
                        it.updateProgress(i, max)
                        it.updateMessage("$i / $max")
                        sleep(100)
                        if( i == 20 ) {
                            throw RuntimeException("Exception test")
                        }
                    }
                }
//                .setOnFail { e -> Dialog.error(e) }
                .setOnDone {
                    Dialog.alert("work done - async")
                }
        }}
        prefWidth = 300.0
        prefHeight = 200.0
    }
}