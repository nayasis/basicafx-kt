package com.github.nayasis.kotlin.javafx.stage

import javafx.stage.Stage
import tornadofx.App
import tornadofx.FXTask
import tornadofx.launch
import java.lang.Thread.sleep
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    launch<ProgressDialogTest>(args)
}

class ProgressDialogTest: App(stylesheet=MyStylesheet::class) {
    override fun start(stage: Stage) {

        ProgressDialog(FXTask {
            val max = 40
            for (i in 1..max) {
                println( "$i to $max" )
                updateProgress(i.toLong(), max.toLong())
                updateMessage("$i / $max")
                updateTitle("title : $i")
                sleep(100)
            }
        }).apply {
            title = "header"
            message = "content"
        }.runSync()

        println(">> sync")

        ProgressDialog(FXTask {
            val max = 40
            for (i in 1..max) {
                println( "$i to $max" )
                updateProgress(i.toLong(), max.toLong())
                updateMessage("$i / $max")
                updateTitle("title : $i")
                sleep(100)
            }
            exitProcess(0)
        }).apply {
            title = "header"
            message = "content"
        }.runAsync()

        println(">> async")

    }
}