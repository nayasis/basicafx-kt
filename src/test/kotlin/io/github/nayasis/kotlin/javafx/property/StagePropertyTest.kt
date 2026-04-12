package io.github.nayasis.kotlin.javafx.property

import io.github.nayasis.kotlin.javafx.stage.watchMaximized
import javafx.application.Application
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.ChoiceBox
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.Stage
import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

fun main(vararg args: String) {
    Application.launch(StagePropertyTest::class.java, *args)
}

class StagePropertyTest: Application() {

    private val propertyFile = File(System.getProperty("user.dir"), "build/manual-test/stage-property-test.bin")

    override fun start(stage: Stage) {
        val locationLabel = Label("saved state: ${propertyFile.absolutePath}")
        val restoreLabel = Label("saved state not found")

        val root = VBox(12.0).apply {
            padding = Insets(16.0)
            children.addAll(
                Label("Move, resize, or maximize this window and close it."),
                Label("Run the app again to check whether StageProperty restores the previous state."),
                locationLabel,
                restoreLabel,
                TextField("sample text").apply {
                    id = "sampleTextField"
                    promptText = "TextField value is stored too"
                },
                TextArea("sample text area").apply {
                    id = "sampleTextArea"
                    promptText = "TextArea value is stored too"
                    prefRowCount = 4
                    VBox.setVgrow(this, Priority.ALWAYS)
                },
                CheckBox("check state restore").apply {
                    id = "sampleCheckBox"
                    isSelected = true
                },
                ComboBox<String>().apply {
                    id = "sampleComboBox"
                    items.addAll("Alpha", "Beta", "Gamma")
                    selectionModel.select(1)
                },
                ChoiceBox<String>().apply {
                    id = "sampleChoiceBox"
                    items.addAll("One", "Two", "Three")
                    selectionModel.select(0)
                },
                Button("Delete saved state").apply {
                    setOnAction {
                        if (propertyFile.exists()) {
                            propertyFile.delete()
                        }
                        restoreLabel.text = "saved state deleted"
                    }
                },
            )
        }

        stage.title = "StageProperty Restore Test"
        stage.scene = Scene(root, 700.0, 500.0)
        stage.watchMaximized()
        stage.show()

        loadStageProperty()?.let {
            it.bind(stage)
            restoreLabel.text = "saved state restored"
        }

        stage.setOnCloseRequest {
            saveStageProperty(StageProperty(stage))
        }

    }

    private fun loadStageProperty(): StageProperty? {
        if (!propertyFile.exists()) {
            return null
        }
        return runCatching {
            ObjectInputStream(propertyFile.inputStream().buffered()).use {
                it.readObject() as StageProperty
            }
        }.getOrNull()
    }

    private fun saveStageProperty(property: StageProperty) {
        propertyFile.parentFile.mkdirs()
        ObjectOutputStream(propertyFile.outputStream().buffered()).use {
            it.writeObject(property)
        }
    }

}
