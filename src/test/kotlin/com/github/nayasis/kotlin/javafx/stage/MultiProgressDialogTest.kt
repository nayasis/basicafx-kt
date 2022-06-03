package com.github.nayasis.kotlin.javafx.stage

import javafx.stage.Stage
import tornadofx.App
import tornadofx.FXTask
import tornadofx.launch
import java.lang.Thread.sleep
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    launch<MultiProgressDialogTest>(args)
}

class MultiProgressDialogTest: App(stylesheet=MyStylesheet::class) {
    override fun start(stage: Stage) {

        val task: (dialog: MultiProgressDialog) -> Unit = { dialog ->
            val max = 40
            for (i in 0..2) {
                for (j in 1..max) {
                    println("$i/$j to $max")
                    dialog.updateProgress(i,j,max)
                    dialog.updateMessage(i,"title[$i] : $j / $max")
                    dialog.updateTitle("title[$i] : $j / $max")
                    sleep(100)
                }
            }
            println(">> done")
        }

        println(">> async")
        MultiProgressDialog(3,task).apply {
            title = "header"
        }.runAsync()

        println(">> sync")
        MultiProgressDialog(3,task).apply {
            title = "header"
        }.runSync()


    }

}