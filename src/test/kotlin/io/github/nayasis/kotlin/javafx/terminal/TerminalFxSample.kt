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

private val logger = KotlinLogging.logger {}

fun main(vararg args: String) {
    launch<TerminalFxSample>(*args)
}

class TerminalFxSample: App(TerminalFxSampleView::class) {
    override fun start(stage: Stage) {
        super.start(stage)
        // set window size to 1000 x 700 for better terminal display
        stage.apply {
            width     = 800.0
            height    = 600.0
            minWidth  = 200.0
            minHeight = 50.0
        }

    }
}

class TerminalFxSampleView : View("JediTermFx Sample") {

    private var terminalWidget = createTerminal()

    override val root = vbox(spacing = 0) {
        terminalWidget.pane.also {
            it.prefWidthProperty().bind(widthProperty())
            it.prefHeightProperty().bind(heightProperty())
            it.minWidthProperty().bind(minWidthProperty())
            it.minHeightProperty().bind(minHeightProperty())
            it.maxWidthProperty().bind(maxWidthProperty())
            it.maxHeightProperty().bind(maxHeightProperty())
        }
        terminalWidget.pane.attachTo(this)
        runLater {
            runAsync{
                terminalWidget.ttyConnector.waitFor()
                runLater {
                    titleProperty.set("Done ${titleProperty.get()}")
                }
            }
        }
    }

    override fun onUndock() {
        terminalWidget.close()
        super.onUndock()
    }
}

private fun createTerminal(): JediTermFxWidget {
    return JediTermFxWidget(100, 100, CustomSettingsProvider()).apply {
        ttyConnector = createTtyConnector()
        addHyperlinkFilter(DefaultHyperlinkFilter())
        start()
    }
}

class CustomSettingsProvider : DefaultSettingsProvider() {
    override fun getTerminalFontSize(): Float {
        return 11.0f
    }

    override fun getLineSpacing(): Float {
        return 1.2f  // 줄 간격을 1.0으로 설정
    }
}

private fun createTtyConnector(): PtyProcessTtyConnector {
    // Windows command example
    val command = listOf("cmd.exe")
//    val command = listOf("src/test/resources/test-program/test.exe", "10")
    val envs = System.getenv().toMutableMap().apply {
        put("TERM", "xterm-256color")
    }

    val process = PtyProcessBuilder()
        .setCommand(command.toTypedArray())
        .setEnvironment(envs)
        .start()

    return PtyProcessTtyConnector(process, StandardCharsets.UTF_8)
}