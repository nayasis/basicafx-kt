package com.github.nayasis.kotlin.javafx.misc

import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Playwright
import org.junit.jupiter.api.Test
import java.nio.file.Files
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively

class TestPlaywright {
    @OptIn(ExperimentalPathApi::class)
    @Test
//    @Disabled("Playwright requires a browser to be installed and configured.")
    fun basic() {

        val tempDir = Files.createTempDirectory("playwright_tmp_data_")

        runCatching {
            Playwright.create().use { playwright ->
                playwright.chromium().launchPersistentContext(tempDir, BrowserType.LaunchPersistentContextOptions().apply {
                    setIgnoreHTTPSErrors(true)
                    setHeadless(true)
                }).use { browser ->
                    val page = browser.newPage()
                    page.navigate("https://www.google.com")
                    println(page.content())
                }
            }
        }.also {
            runCatching { tempDir.deleteRecursively() }
        }

    }
}