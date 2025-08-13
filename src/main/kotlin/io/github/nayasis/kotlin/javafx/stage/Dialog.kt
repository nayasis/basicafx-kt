@file:Suppress("unused")

package io.github.nayasis.kotlin.javafx.stage

import io.github.nayasis.kotlin.basica.core.extension.ifEmpty
import io.github.nayasis.kotlin.basica.core.extension.ifNotEmpty
import io.github.nayasis.kotlin.basica.core.extension.ifNotNull
import io.github.nayasis.kotlin.basica.core.io.Paths
import io.github.nayasis.kotlin.basica.core.io.div
import io.github.nayasis.kotlin.basica.core.io.exists
import io.github.nayasis.kotlin.basica.core.io.isReadable
import io.github.nayasis.kotlin.basica.core.string.toPath
import io.github.nayasis.kotlin.basica.etc.Platforms
import io.github.nayasis.kotlin.basica.etc.error
import io.github.nayasis.kotlin.javafx.stage.progress.MultiProgressDialog
import io.github.nayasis.kotlin.javafx.stage.progress.ProgressDialog
import io.github.oshai.kotlinlogging.KotlinLogging
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.Alert.AlertType.*
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
import tornadofx.FileChooserMode
import tornadofx.FileChooserMode.*
import java.io.File
import java.nio.file.Path
import kotlin.Double.Companion.MAX_VALUE

private val logger = KotlinLogging.logger {}

class Dialog { companion object {

    @Suppress("MemberVisibilityCanBePrivate")
    val defaultCss = ArrayList<String>()

    @Suppress("MemberVisibilityCanBePrivate")
    var defaultTitle: String? = null

    fun dialog(type: AlertType, message: String? = null): Alert {
        return Alert(type).apply {
            headerText = null
            contentText = message
            initModality(WINDOW_MODAL)
            initOwner(Stages.focusedWindow)
            with(dialogPane.scene.window as Stage) {
                isAlwaysOnTop = true
                loadDefaultIcon()
            }
            defaultTitle.ifNotNull { title = it }
            defaultCss.ifEmpty{ owner?.scene?.stylesheets }.ifEmpty{ owner?.scene?.root?.stylesheets }.ifNotEmpty {
                dialogPane.scene.stylesheets.addAll(it)
            }
        }
    }

    fun alert(message: String?, content: String? = null, expanded: Boolean = true) {
        dialog(INFORMATION, message).expand(content,expanded).showAndWait()
    }

    fun confirm(message: String?, content: String? = null, expanded: Boolean = true): Boolean {
        return dialog(CONFIRMATION, message).apply {
            buttonTypes.map { dialogPane.lookupButton(it) }.forEach {
                it.addEventHandler(KeyEvent.KEY_PRESSED) { e ->
                    if (e.code == KeyCode.ENTER && e.target is Button)
                        (e.target as Button).fire()
                }
            }
        }.expand(content,expanded).showAndWait().get() == ButtonType.OK
    }

    fun error(message: String?, exception: Throwable? = null, expanded: Boolean = false) {
        dialog(ERROR, message ?: exception?.message).apply {
            if( exception != null ) {
                logger.error(exception)
                expand(exception.stackTraceToString(),expanded)
            }
        }.showAndWait()
    }

    fun error(exception: Throwable?, expanded: Boolean = false) {
        error(exception?.message, exception,expanded)
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
            defaultTitle.ifNotNull { title = it }
            defaultCss.ifEmpty{ owner?.scene?.stylesheets }.ifEmpty{ owner?.scene?.root?.stylesheets }.ifNotEmpty {
                dialogPane.scene.stylesheets.addAll(it)
            }
        }.showAndWait()
        return if(res.isEmpty) null else res.get()
    }

    fun progress(title: String? = null, async: Boolean = true, task: ((dialog: ProgressDialog) -> Unit)? = null): ProgressDialog {
        return ProgressDialog(title).apply{
            initOwner(Stages.focusedWindow)
            if(async) runAsync(task) else runSync(task)
        }
    }

    fun progressMulti(progressCount: Int, title: String? = null, async: Boolean = true, task: ((dialog: MultiProgressDialog) -> Unit)? = null): MultiProgressDialog {
        return MultiProgressDialog(progressCount,title).apply{
            initOwner(Stages.focusedWindow)
            if(async) runAsync(task) else runSync(task)
        }
    }

    fun filePicker(title: String = "", extension: String = "", description: String = "", initialDirectory: Path? = null, mode: FileChooserMode = Single, owner: Window? = null, option: FileChooser.() -> Unit = {}): List<Path> {
        val chooser = FileChooser().apply {
            this.title = title
            this.extensionFilters.add( ExtensionFilter(description.ifEmpty{extension}, extension.split(",;")) )
            this.initialDirectory = if( initialDirectory.exists() && initialDirectory != "".toPath() && initialDirectory.isReadable() ) initialDirectory!!.toFile() else dirDesktop()
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
        }.map { it.toPath() }
    }

    fun dirPicker(title: String = "", initialDirectory: Path? = null, owner: Window? = null, option: DirectoryChooser.() -> Unit = {}): Path? {
        val chooser = DirectoryChooser().apply {
            this.title = title
            this.initialDirectory = if( initialDirectory.exists() && initialDirectory != "".toPath() && initialDirectory.isReadable() ) initialDirectory!!.toFile() else dirDesktop()
        }
        option(chooser)
        return chooser.showDialog(owner)?.toPath()
    }

    private fun dirDesktop(): File {
        return Paths.userHome.let { when{
            Platforms.isWindows -> it / "Desktop"
            else -> it
        }}.toFile()
    }

}}

fun Alert.content(content: String?): Alert {
    dialogPane.contentText = content
    return this
}

fun Alert.expand(content: String?, expanded: Boolean = false): Alert {
    return expand( if(content.isNullOrEmpty()) null else
        GridPane().apply {
            maxWidth = MAX_VALUE
            add(TextArea(content).apply {
                isEditable = false
                maxWidth  = MAX_VALUE
                maxHeight = MAX_VALUE
                GridPane.setVgrow(this,ALWAYS)
                GridPane.setHgrow(this,ALWAYS)
            },0,0)
        },
        expanded
    )
}

fun Alert.content(content: Node?): Alert {
    dialogPane.content = content
    return this
}

fun Alert.expand(content: Node?, expanded: Boolean = false): Alert {
    dialogPane.apply {
        expandableContent = content
        expandedProperty().value = expanded
    }
    return this
}