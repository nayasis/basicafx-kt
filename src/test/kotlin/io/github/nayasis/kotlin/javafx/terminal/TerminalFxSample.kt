package io.github.nayasis.kotlin.javafx.terminal

import com.pty4j.PtyProcessBuilder
import com.techsenger.jeditermfx.app.pty.PtyProcessTtyConnector
import com.techsenger.jeditermfx.core.TerminalColor
import com.techsenger.jeditermfx.ui.DefaultHyperlinkFilter
import com.techsenger.jeditermfx.ui.JediTermFxWidget
import com.techsenger.jeditermfx.ui.settings.DefaultSettingsProvider
import io.github.oshai.kotlinlogging.KotlinLogging
import javafx.stage.Stage
import tornadofx.*
import java.nio.charset.StandardCharsets
import org.slf4j.LoggerFactory
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import javafx.scene.layout.Pane

private val logger = KotlinLogging.logger {}

fun main(vararg args: String) {
    configureLogging()
    launch<TerminalFxSample>(*args)
}

private fun configureLogging() {
    listOf(
        "com.techsenger.jeditermfx.core.emulator.JediEmulator",
        "com.techsenger.jeditermfx.core",
        "com.techsenger.jeditermfx.ui",
        "com.techsenger.jeditermfx",
        "com.pty4j"
    ).forEach { packageName ->
        (LoggerFactory.getLogger(packageName) as Logger).level = Level.WARN
    }
}

class TerminalFxSample: App(TerminalFxSampleView::class) {
    override fun start(stage: Stage) {
        super.start(stage)
        // set window size for better terminal display with Korean text
        stage.apply {
            width     = 400.0
            height    = 250.0
            minWidth  = 100.0
            minHeight =  40.0
        }

    }
}

class TerminalFxSampleView : View("JediTermFx Sample") {

    private var terminalWidget = createTerminal()

    override val root = vbox(spacing = 0) {
        terminalWidget.pane.also {
            it.bindSizeProperties(this@vbox)
        }
        terminalWidget.pane.attachTo(this)
        runLater {
            runAsync{
                terminalWidget.ttyConnector.waitFor()
                runLater {
                    titleProperty.set("Done - ${titleProperty.get()}")
                }
            }
        }
    }

    override fun onUndock() {
        terminalWidget.close()
        super.onUndock()
    }

    private fun Pane.bindSizeProperties(other: Pane) {
        prefWidthProperty().bind(other.widthProperty())
        prefHeightProperty().bind(other.heightProperty())
        minWidthProperty().bind(other.minWidthProperty())
        minHeightProperty().bind(other.minHeightProperty())
        maxWidthProperty().bind(other.maxWidthProperty())
        maxHeightProperty().bind(other.maxHeightProperty())
    }

}

private fun createTerminal(): JediTermFxWidget {
    return JediTermFxWidget(80, 200, CustomSettingsProvider()).apply {  // 더 넓은 터미널
        ttyConnector = createTtyConnector()
        addHyperlinkFilter(DefaultHyperlinkFilter())
        start()
    }
}

class CustomSettingsProvider : DefaultSettingsProvider() {
    override fun getTerminalFontSize(): Float {
        return 10.2f
    }

    override fun getLineSpacing(): Float {
        return 1.2f
    }
}

private fun createTtyConnector(): PtyProcessTtyConnector {
    // Windows command example
    val command = listOf("cmd")
//    val command = listOf("src/test/resources/test-program/test.exe", "100")
    val envs = System.getenv().toMutableMap().apply {
        put("TERM", "xterm-256color")
    }

    val process = PtyProcessBuilder()
        .setCommand(command.toTypedArray())
        .setEnvironment(envs)
        .start()

    return PtyProcessTtyConnector(process, StandardCharsets.UTF_8)
}