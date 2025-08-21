package io.github.nayasis.kotlin.javafx.terminal

import com.pty4j.PtyProcessBuilder
import com.techsenger.jeditermfx.app.pty.PtyProcessTtyConnector
import com.techsenger.jeditermfx.core.TerminalColor
import com.techsenger.jeditermfx.ui.DefaultHyperlinkFilter
import com.techsenger.jeditermfx.ui.JediTermFxWidget
import com.techsenger.jeditermfx.ui.settings.DefaultSettingsProvider
import io.github.oshai.kotlinlogging.KotlinLogging
import javafx.scene.control.Label
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
        // 화면 크기를 300x200으로 설정
        stage.apply {
            width     = 300.0
            height    = 200.0
            minWidth  = 300.0
            minHeight = 200.0
        }
    }
}

class TerminalFxSampleView : View("TerminalFx Sample") {

    private lateinit var titleLabel: Label

    override val root = vbox {
        titleLabel = label("This is a sample terminal application using JavaFX.")
        button("Click Me") {
            action {
                val widget = createTerminal()
                widget.pane.attachTo(this@vbox)

                widget.ttyConnector.waitFor()

                // 명령어 실행 완료 후 제목에 "done" 표시
                titleLabel.text = "This is a sample terminal application using JavaFX. - done"
            }
        }
    }

}

private fun createTerminal(): JediTermFxWidget {
    return JediTermFxWidget(80, 24, DarkThemeSettingsProvider()).apply {
        ttyConnector = createTtyConnector()
        addHyperlinkFilter(DefaultHyperlinkFilter())
        start()
    }
}

class DarkThemeSettingsProvider : DefaultSettingsProvider() {
    override fun getDefaultBackground(): TerminalColor {
        return TerminalColor(0, 0, 0)
    }

    override fun getDefaultForeground(): TerminalColor {
        return TerminalColor(255, 255, 255)
    }
}

private fun createTtyConnector(): PtyProcessTtyConnector {
    try {
        val command = listOf("ls", "-al")
        val envs = HashMap<String,String>(System.getenv()).apply {
            put("TERM", "xterm-256color")
        }
        val process = PtyProcessBuilder().setCommand(command.toTypedArray()).setEnvironment(envs).start()
        return PtyProcessTtyConnector(process, StandardCharsets.UTF_8)
    } catch (e: Exception) {
        throw IllegalStateException(e)
    }
}