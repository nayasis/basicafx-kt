package io.github.nayasis.kotlin.javafx.misc

import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import java.io.Closeable

class WebBrowser(
    options: List<String> = listOf(
        "--ignore-ssl-errors",
        "--ignore-certificate-errors",
        "--disable-web-security",
        "--disable-features=VizDisplayCompositor",
        "--no-sandbox",
        "--disable-dev-shm-usage",
    ),
): Closeable {

    private val playwright = Playwright.create()
    private val browser    = playwright.chromium().launch(
        BrowserType.LaunchOptions()
            .setArgs(options)
            .setHeadless(true)
    )

    /**
     * Creates a new page and uses it with a lambda function, then automatically cleans up.
     *
     * @param action lambda function that defines the work to be done with the page
     * @return result of the lambda function
     */
    fun <T> withPage(action: (Page) -> T): T {
        return browser.newPage().use { page ->
            action(page)
        }
    }

    override fun close() {
        runCatching { browser?.close() }
        runCatching { playwright?.close() }
    }

}

