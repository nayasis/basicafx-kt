package io.github.nayasis.kotlin.javafx.stage.progress

import com.github.nayasis.kotlin.javafx.stage.Dialog
import javafx.stage.Stage
import tornadofx.App
import tornadofx.launch
import java.lang.Thread.sleep
import kotlin.math.floor
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    launch<MultiProgressDialogTest>(args)
}

class MultiProgressDialogTest: App() {
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