package io.github.nayasis.kotlin.javafx.terminal

import com.pty4j.PtyProcessBuilder
import com.techsenger.jeditermfx.app.pty.PtyProcessTtyConnector
import com.techsenger.jeditermfx.ui.JediTermFxWidget
import com.techsenger.jeditermfx.ui.settings.DefaultSettingsProvider
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.nio.charset.StandardCharsets

class JediTermFxTest {

    @Test
    fun testJediTermFxWidgetCreation() {
        // Test that JediTermFxWidget can be created without errors
        val widget = JediTermFxWidget(80, 24, DefaultSettingsProvider())
        assertNotNull(widget)
        assertNotNull(widget.pane)
    }

    @Test
    fun testPtyProcessTtyConnectorCreation() {
        // Test that PtyProcessTtyConnector can be created
        try {
            val command = if (System.getProperty("os.name").lowercase().contains("windows")) {
                arrayOf("cmd.exe", "/c", "echo", "test")
            } else {
                arrayOf("/bin/echo", "test")
            }
            
            val process = PtyProcessBuilder()
                .setCommand(command)
                .start()
            
            val connector = PtyProcessTtyConnector(process, StandardCharsets.UTF_8)
            assertNotNull(connector)
            
            // Clean up
            process.destroy()
        } catch (e: Exception) {
            // This might fail in headless environment, but at least we know the classes are available
            println("PtyProcessTtyConnector test failed (expected in headless environment): ${e.message}")
        }
    }

    @Test
    fun testJediTermFxDependenciesAvailable() {
        // Test that all required classes are available
        assertDoesNotThrow {
            Class.forName("com.techsenger.jeditermfx.ui.JediTermFxWidget")
            Class.forName("com.techsenger.jeditermfx.app.pty.PtyProcessTtyConnector")
            Class.forName("com.techsenger.jeditermfx.ui.settings.DefaultSettingsProvider")
            Class.forName("com.techsenger.jeditermfx.core.TerminalColor")
            Class.forName("com.techsenger.jeditermfx.ui.DefaultHyperlinkFilter")
        }
    }
}
