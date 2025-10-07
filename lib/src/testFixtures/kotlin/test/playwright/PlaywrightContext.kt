package test.playwright

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestScope
import java.util.concurrent.ConcurrentHashMap

class PlaywrightContext(val playwright: Playwright, val browser: Browser) {
    companion object {
        fun TestScope.getPlaywright(playwrightContext: PlaywrightContext) = playwrightContext.playwright

        fun TestScope.getBrowser(playwrightContext: PlaywrightContext) = playwrightContext.browser

        fun TestScope.getBrowserContext(playwrightContext: PlaywrightContext) =
            playwrightContext.getBrowserContext(testCase)
                ?: throw IllegalStateException("BrowserContext not found for test case: ${testCase.name}")

        fun TestScope.getPage(playwrightContext: PlaywrightContext) =
            playwrightContext.getPage(testCase)
                ?: throw IllegalStateException("Page not found for test case: ${testCase.name}")
    }

    data class TestContext(
        val browserContext: BrowserContext,
        val page: Page,
    )

    private val contexts = ConcurrentHashMap<TestCase, TestContext>()

    fun getBrowserContext(testCase: TestCase) = contexts[testCase]?.browserContext

    fun getPage(testCase: TestCase) = contexts[testCase]?.page

    internal fun addTestContext(
        testCase: TestCase,
        context: BrowserContext,
        page: Page,
    ) {
        contexts[testCase] = TestContext(context, page)
    }

    internal fun getTestContext(testCase: TestCase) = contexts[testCase]
}
