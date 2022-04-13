package com.github.nayasis.kotlin.javafx.stage

import com.github.nayasis.kotlin.basica.core.extention.isNotEmpty
import com.github.nayasis.kotlin.basica.core.path.Paths
import com.github.nayasis.kotlin.basica.core.path.div
import com.github.nayasis.kotlin.basica.etc.Platforms
import com.github.nayasis.kotlin.basica.etc.error
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.Button
import javafx.scene.control.ButtonType
import javafx.scene.control.TextArea
import javafx.scene.control.TextInputDialog
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority.ALWAYS
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import javafx.stage.FileChooser.ExtensionFilter
import javafx.stage.Modality.WINDOW_MODAL
import javafx.stage.Stage
import javafx.stage.Window
import mu.KotlinLogging
import tornadofx.FXTask
import tornadofx.FileChooserMode
import tornadofx.FileChooserMode.*
import tornadofx.hgrow
import tornadofx.vgrow
import java.io.File
import kotlin.Double.Companion.MAX_VALUE

private val logger = KotlinLogging.logger {}

class Dialog { companion object {

    fun dialog(type: AlertType, message: String?): Alert {
        return Alert(type).apply {
            title = null
            headerText = null
            contentText = message
            initModality(WINDOW_MODAL)
            initOwner(Stages.focusedWindow)
            with( dialogPane.scene.window as Stage ) {
                isAlwaysOnTop = true
                loadDefaultIcon()
            }
        }
    }

    fun alert(message: String?, content: String? = null) {
        dialog(AlertType.INFORMATION, message).expand(content).showAndWait()
    }

    fun confirm(message: String?, content: String? = null): Boolean {
        return dialog(AlertType.CONFIRMATION, message).expand(content).apply {
            buttonTypes.map { dialogPane.lookupButton(it) }.forEach {
                it.addEventHandler(KeyEvent.KEY_PRESSED) { e ->
                    if (e.code == KeyCode.ENTER && e.target is Button)
                        (e.target as Button).fire()
                }
            }
        }.showAndWait().get() == ButtonType.OK
    }

    fun error(message: String?, exception: Throwable? = null) {
        dialog(AlertType.ERROR, message ?: exception?.message).apply {
            if( exception != null ) {
                logger.error(exception)
                expand(exception.stackTraceToString())
            }
        }.showAndWait()
    }

    fun error(exception: Throwable?) {
        error(exception?.message, exception)
    }

    fun prompt(message: String?, defaultValue: String? = null): String? {
        val res = TextInputDialog(defaultValue).apply {
            headerText = message
            initModality(WINDOW_MODAL)
            initOwner(Stages.focusedWindow)
            with( dialogPane.scene.window as Stage ) {
                isAlwaysOnTop = true
                loadDefaultIcon()
            }
        }.showAndWait()
        return if(res.isEmpty) null else res.get()
    }

    fun progress(title: String? = null, async: Boolean = true, func: (FXTask<*>.() -> Unit)? = null): ProgressDialog {
        val task = func?.let {FXTask(func=it)}
        return ProgressDialog(task).apply{
            initOwner(Stages.focusedWindow)
            this.title = title
            if(async) runAsync() else runSync()
        }
    }

    fun progress(title: String? = null, async: Boolean = true, task: FXTask<*>): ProgressDialog {
        return ProgressDialog(task).apply{
            initOwner(Stages.focusedWindow)
            this.title = title
            if(async) runAsync() else runSync()
        }
    }

    fun filePicker(title: String = "", extension: String = "", description: String = "", initialDirectory: File? = null, mode: FileChooserMode = Single, owner: Window? = null, option: FileChooser.() -> Unit = {}): List<File> {
        val chooser = FileChooser().apply {
            this.title = title
            this.extensionFilters.add( ExtensionFilter(description.ifEmpty{extension}, extension.split(",;")) )
            this.initialDirectory = if( initialDirectory != null && initialDirectory.exists() && initialDirectory.canRead() ) {
                initialDirectory
            } else {
                dirDesktop()
            }
        }
        option(chooser)
        return when(mode) {
            Single -> {
                val result = chooser.showOpenDialog(owner)
                if (result == null) emptyList() else listOf(result)
            }
            Multi -> chooser.showOpenMultipleDialog(owner) ?: emptyList()
            Save -> {
                val result = chooser.showSaveDialog(owner)
                if (result == null) emptyList() else listOf(result)
            }
            else -> emptyList()
        }


    }

    fun dirPicker(title: String = "", initialDirectory: File? = null, owner: Window? = null, option: DirectoryChooser.() -> Unit = {}): File? {
        val chooser = DirectoryChooser().apply {
            this.title = title
            this.initialDirectory = if( initialDirectory != null && initialDirectory.exists() && initialDirectory.canRead() ) {
                initialDirectory
            } else {
                dirDesktop()
            }
        }
        option(chooser)
        return chooser.showDialog(owner)
    }

    private fun dirDesktop(): File {
        return Paths.userHome.let {
            when{
                Platforms.isWindows -> it / "Desktop"
                else -> it
            }
        }.toFile()
    }

}}

fun Alert.expand(content: String?): Alert {
    if(content.isNotEmpty()) {
        dialogPane.expandableContent = GridPane().apply {
            maxWidth = MAX_VALUE
            add(TextArea(content).apply {
                isEditable = false
                maxWidth = MAX_VALUE
                maxHeight = MAX_VALUE
                vgrow = ALWAYS
                hgrow = ALWAYS
            },0,0)
        }
    }
    return this
}