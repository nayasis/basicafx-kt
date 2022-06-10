package com.github.nayasis.kotlin.javafx.stage.progress

import com.github.nayasis.kotlin.basica.core.path.name
import com.github.nayasis.kotlin.basica.core.path.statistics
import com.github.nayasis.kotlin.basica.core.string.toPath
import com.github.nayasis.kotlin.java.file.copyTree
import com.github.nayasis.kotlin.javafx.stage.Dialog
import javafx.stage.Stage
import tornadofx.App
import tornadofx.launch
import tornadofx.runLater
import java.lang.Thread.sleep
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    launch<ProgressCopyTest>(args)
}

class ProgressCopyTest: App() {
    override fun start(stage: Stage) {

        val src = "c:/download".toPath()
        val trg = "c:/download-temp".toPath()

        val res = src.statistics

        Dialog.progressMulti(2,"Progress copy") { dialog ->
            copyTree(src,trg) { index, file, fileRead, fileSize ->
                dialog.updateProgress(0,index,res.fileCount)
                dialog.updateSubMessage(0,"%.2f%".format(dialog.getProgress(0) * 100))
                dialog.updateMessage(1,file.name)
                dialog.updateProgress(1,fileRead,fileSize)
                dialog.updateSubMessage(1,"%.2f%".format(dialog.getProgress(1) * 100))
            }
            runLater {
                Dialog.alert("Done !!")
            }
        }

    }
}