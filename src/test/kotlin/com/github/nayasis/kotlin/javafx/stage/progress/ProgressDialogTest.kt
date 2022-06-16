package com.github.nayasis.kotlin.javafx.stage.progress

import com.github.nayasis.kotlin.javafx.stage.Dialog
import javafx.stage.Stage
import tornadofx.App
import tornadofx.launch
import tornadofx.runLater
import java.lang.Thread.sleep
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    launch<SingleProgressDialogTest>(args)
}

class SingleProgressDialogTest: App() {
    override fun start(stage: Stage) {

        println(">> sync")
        ProgressDialog("header").apply {
            onSuccess = {
                Dialog.alert("success !")
            }
            onFail = { e ->
                Dialog.error(e)
            }
            onDone = {
//                Dialog.alert("work done")
            }
        }.runSync {
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

//        println(">> async")
//        ProgressDialog("header").runAsync {
//            val max = 40
//            for (i in 1..max) {
//                println( "$i to $max" )
//                it.updateProgress(i, max)
//                it.updateMessage("$i / $max")
//                sleep(100)
//            }
//            exitProcess(0)
//        }

    }
}