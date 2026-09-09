package io.github.nayasis.kotlin.javafx.stage.progress

import io.github.nayasis.kotlin.javafx.app.FxView
import io.github.nayasis.kotlin.javafx.common.DemoApp
import io.github.nayasis.kotlin.javafx.stage.Dialog
import javafx.application.Application
import javafx.scene.control.Button
import javafx.scene.layout.VBox
import java.lang.Thread.sleep

fun main(args: Array<String>) {
    Application.launch(ProgressDialogTest::class.java, *args)
}

class ProgressDialogTest: DemoApp(ProgressDialogView())

/** counts to 40 and fails halfway, to exercise the failure path */
private val work: (dialog: ProgressDialog) -> Unit = { dialog ->
    val max = 40
    for (i in 1..max) {
        println( "$i to $max" )
        dialog.updateProgress(i, max)
        dialog.updateMessage("$i / $max")
        sleep(100)
        if( i == 20 ) {
            throw RuntimeException("Exception test")
        }
    }
}

class ProgressDialogView: FxView("progress dialog test") {

    override val root = VBox().apply {

        children.add(Button("sync").apply { setOnAction {
            ProgressDialog("header")
                .setOnDone { Dialog.alert("work done - sync") }
                .runSync(work)
        }})

        children.add(Button("async").apply { setOnAction {
            ProgressDialog("header")
                .runAsync(work)
                .setOnDone { Dialog.alert("work done - async") }
        }})

        prefWidth = 300.0
        prefHeight = 200.0

    }

}
