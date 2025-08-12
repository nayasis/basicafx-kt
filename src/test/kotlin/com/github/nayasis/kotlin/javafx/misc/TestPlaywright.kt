package com.github.nayasis.kotlin.javafx.misc

import com.github.nayasis.kotlin.javafx.common.createTempDir
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Playwright
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.nio.file.Files
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createTempDirectory
import kotlin.io.path.deleteRecursively

class TestPlaywright {
    @OptIn(ExperimentalPathApi::class)
    @Test
    @Disabled("Playwright requires a browser to be installed and configured.")
    fun basic() {
        val tempDir = createTempDir("playwright_tmp_data_")
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
        }
    }
}