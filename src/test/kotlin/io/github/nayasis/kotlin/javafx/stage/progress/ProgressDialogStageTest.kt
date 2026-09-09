package io.github.nayasis.kotlin.javafx.stage.progress

import javafx.application.Application
import javafx.stage.Stage

fun main(args: Array<String>) {
    Application.launch(ProgressDialogStageTest::class.java, *args)
}

class ProgressDialogStageTest: Application() {
    override fun start(stage: Stage) {
        ProgressDialogStage(2).apply {
            updateTitle("Title12312312312312312312312312123123123123123123123123123123123123123123132")
            updateMessage(0,"message 01")
            updateMessage(1,"message 02")
            updateSubMessage(0,"33%")
            updateSubMessage(1,"66%")
            updateProgress(0,10,30)
            updateProgress(1,20,30)
        }.show()
    }
}
