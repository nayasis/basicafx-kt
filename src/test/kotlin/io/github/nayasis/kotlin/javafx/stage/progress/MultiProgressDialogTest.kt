package io.github.nayasis.kotlin.javafx.stage.progress

import io.github.nayasis.kotlin.javafx.stage.Dialog
import javafx.application.Application
import javafx.stage.Stage
import java.lang.Thread.sleep
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    Application.launch(MultiProgressDialogTest::class.java, *args)
}

class MultiProgressDialogTest: Application() {
    override fun start(stage: Stage) {

        val task: (dialog: MultiProgressDialog) -> Unit = { dialog ->
            val max = 40
            for (i in 0..2) {
                for (j in 1..max) {
                    println("$i/$j to $max")
                    dialog.updateProgress(i,j,max)
                    dialog.updateMessage(i,"title[$i] : $j / $max")
                    dialog.updateSubMessage(i,"${ (dialog.getProgress(i)*100).toInt()}%")
                    dialog.updateTitle("title[$i] : $j / $max")
                    sleep(100)
                }
            }
            println(">> done")
        }

        println(">> async")
        MultiProgressDialog(3,"header").runAsync(task)

        println(">> sync")
        MultiProgressDialog(3,"header").runSync(task)

        Dialog.alert("done !")
        exitProcess(0)

    }

}