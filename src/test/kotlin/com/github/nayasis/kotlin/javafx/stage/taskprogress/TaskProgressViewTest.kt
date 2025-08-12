package com.github.nayasis.kotlin.javafx.stage.taskprogress

import com.github.nayasis.kotlin.javafx.geometry.Insets
import io.github.oshai.kotlinlogging.KotlinLogging
import javafx.concurrent.Service
import javafx.concurrent.Task
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.stage.Modality
import javafx.stage.Popup
import javafx.stage.Stage
import org.controlsfx.control.Notifications
import org.controlsfx.control.TaskProgressView
import org.controlsfx.dialog.ProgressDialog
import tornadofx.*
import java.util.concurrent.Executors
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}

fun main() {
    launch<TaskProgressViewTest>()
}

class TaskProgressViewTest: App(TaskProgressViewer::class)

class TaskProgressViewer: View("task progress view") {

    override val root = vbox {
        button("Run singleTaskService()").setOnAction {
            singleTaskService()
        }
        button("Run multipleTasksExecutorOnStage()").setOnAction {
            multipleTasksExecutorOnStage()
        }
        button("Run multipleTasksExecutorPopup()").setOnAction {
            multipleTasksExecutorPopup()
        }
        spacing    = 5.0
        alignment  = Pos.CENTER
        padding    = Insets(15)
        prefWidth  = 300.0
        prefHeight = 250.0
    }

    fun task(fn: () -> Unit = {
        for(i in 0..99) {
            logger.debug { "Found ${i+1} friends!" }
            Thread.sleep(300)
        }
    }): Task<Void?> {
        return object: Task<Void?>() {
            override fun call(): Void? {
                try {
                    Thread.sleep(ThreadLocalRandom.current().nextInt(200, 2000).toLong())
                } catch (e: InterruptedException) {
                    //e.printStackTrace();
                }
                updateMessage("Finding friends . . .")
                updateProgress(0, 100)
                fn.invoke()
//                for (i in 0..99) {
////                    if (isCancelled) {
////                        updateMessage("Cancelled")
////                        break
////                    }
//                    updateProgress((i + 1).toLong(), 100)
//                    updateMessage("Found ${i+1} friends!")
//                    logger.debug { "Found ${i+1} friends!" }
//
//                    try {
//                        Thread.sleep(300)
//                    } catch (interrupted: InterruptedException) {
//                        if (isCancelled) {
//                            updateMessage("Cancelled")
//                            break
//                        }
//                    }
//                }
//                updateMessage("Found all.")
                return null
            }
        }
    }

    private fun singleTaskService() {

        val service = object: Service<Void?>() {
            override fun createTask(): Task<Void?> {
                return task()
            }
        }

        ProgressDialog(service).apply {
            title = "Progress Dialog Title"
            initOwner(primaryStage)
            headerText = "Header Text"
            initModality(Modality.WINDOW_MODAL)
                setOnCloseRequest {
                runLater {
                    Notifications.create()
                        .title("Information")
                        .text("Task done")
                        .showInformation()
                }
            }

        }

        service.start()

    }

    private fun multipleTasksExecutorOnStage() {
        val executorService = Executors.newCachedThreadPool()
        val tasks = ArrayList<Task<Void?>>()
        for (i in 0..9) {
            val task = task()
            executorService.submit(task)
            tasks.add(task)
        }
        val view = TaskProgressView<Task<Void?>>()
        //view.setGraphicFactory(t -> new ImageView(new Image(getClass().getResourceAsStream("/icon.png"))));
        //view.setGraphicFactory(t -> new ImageView(new Image(getClass().getResourceAsStream("/icon.png"))));
        view.tasks.addAll(tasks)


        val dialogStage = Stage().apply {
            title = "Tasks"
            initModality(Modality.WINDOW_MODAL)
            initOwner(primaryStage)
            scene = Scene(view)
            setOnCloseRequest {
                executorService.shutdownNow()
                hide()
            }
            show()
        }
        executorService.shutdown()
        Thread {
            try {
                executorService.awaitTermination(1, TimeUnit.MINUTES)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } finally {
                runLater {
                    dialogStage.hide()
                }
            }
        }.start()
    }

    private fun multipleTasksExecutorPopup() {
        val executorService = Executors.newCachedThreadPool()
        val tasks: MutableList<Task<Void?>> = ArrayList()
        for (i in 0..9) {
            val task = task()
            executorService.submit(task)
            tasks.add(task)
        }
        val view = TaskProgressView<Task<Void?>>()
        //view.setGraphicFactory(t -> new ImageView(new Image(getClass().getResourceAsStream("/icon.png"))));
        view.tasks.addAll(tasks)

        val popup = Popup().apply {
            isAutoFix = true
            isAutoHide = true
            isHideOnEscape = true
            content.add(view)
            show(primaryStage)
        }

        executorService.shutdown()
        Thread {
            try {
                executorService.awaitTermination(1, TimeUnit.MINUTES)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } finally {
                runLater {
                    popup.hide()
                }
            }
        }.start()
    }

}