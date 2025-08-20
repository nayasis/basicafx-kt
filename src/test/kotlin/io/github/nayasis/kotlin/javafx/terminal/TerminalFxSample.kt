package io.github.nayasis.kotlin.javafx.terminal

import com.pty4j.PtyProcessBuilder
import com.techsenger.jeditermfx.app.pty.PtyProcessTtyConnector
import com.techsenger.jeditermfx.core.TerminalColor
import com.techsenger.jeditermfx.core.TtyConnector
import com.techsenger.jeditermfx.ui.JediTermFxWidget
import com.techsenger.jeditermfx.ui.settings.DefaultSettingsProvider
import io.github.oshai.kotlinlogging.KotlinLogging
import tornadofx.*


private val logger = KotlinLogging.logger {}

fun main(vararg args: String) {
    launch<TerminalFxSample>(*args)
}

class TerminalFxSample: App(TerminalFxSampleView::class) {



}

class TerminalFxSampleView : View("TerminalFx Sample") {

    override val root = vbox {
        label("This is a sample terminal application using JavaFX.")
        button("Click Me") {
            action {
                println("Button clicked!")
            }
        }
        textfield {
            promptText = "Type something..."
            action {
                println("You typed: ${text}")
            }
        }
    }
}

private fun createTerminal() {

    JediTermFxWidget(80, 24, DarkThemeSettingsProvider()).apply {
        ttyConnector =

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

private fun createTtyConnector(): TtyConnector {
    try {
        var envs = System.getenv()
        val command: Array<String?>?
        if (Platform.isWindows()) {
            command = arrayOf<String>("cmd.exe")
        } else {
            command = arrayOf<String>("/bin/bash", "--login")
            envs = HashMap<String?, String?>(System.getenv())
            envs.put("TERM", "xterm-256color")
        }
        val process = PtyProcessBuilder().setCommand(command).setEnvironment(envs).start()
        return PtyProcessTtyConnector(process, StandardCharsets.UTF_8)
    } catch (e: Exception) {
        throw IllegalStateException(e)
    }
}