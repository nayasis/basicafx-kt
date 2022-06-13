package com.github.nayasis.kotlin.java.file

import com.github.nayasis.kotlin.basica.core.number.round
import com.github.nayasis.kotlin.basica.core.path.exists
import com.github.nayasis.kotlin.basica.core.path.name
import com.github.nayasis.kotlin.basica.core.path.statistics
import com.github.nayasis.kotlin.basica.core.string.toPath
import com.github.nayasis.kotlin.javafx.stage.Dialog
import com.github.nayasis.kotlin.javafx.stage.progress.SingleProgressDialogTest
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import tornadofx.App
import tornadofx.launch
import java.lang.Thread.sleep

fun main(args: Array<String>) {
    launch<ProgressFileHandlerTest>(args)
}

class ProgressFileHandlerTest: App() {
    override fun start(stage: Stage) {
        val src = "c:/download".toPath()
        val trg = "c:/download-temp".toPath()
        val res = src.statistics
        Dialog.progressMulti(2,"Progress copy") { dialog ->
            copyTree(src,trg) { index, file, fileRead, fileSize ->
                dialog.updateProgress(0,index,res.fileCount)
                dialog.updateSubMessage(0,"%.1f%%".format(dialog.getProgress(0) * 100))
                dialog.updateMessage(1,file.name)
                dialog.updateProgress(1,fileRead,fileSize)
                dialog.updateSubMessage(1,"%.1f%%".format(dialog.getProgress(1) * 100))
                sleep(20)
            }
        }
    }
}