package test.playwright

import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import io.kotest.core.extensions.MountableExtension
import io.kotest.core.listeners.AfterEachListener
import io.kotest.core.listeners.AfterSpecListener
import io.kotest.core.listeners.BeforeEachListener
import io.kotest.core.spec.Spec
import io.kotest.core.test.TestCase
import io.kotest.engine.test.TestResult
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createParentDirectories
import kotlin.io.path.writeText

const val EXTENSION_PAGE_CONTENT = "html"
const val EXTENSION_SCREENSHOT = "png"

class PlaywrightExtension(
    private val config: PlaywrightConfig,
) : MountableExtension<PlaywrightConfig, PlaywrightContext>, BeforeEachListener, AfterEachListener, AfterSpecListener {
    private lateinit var context: PlaywrightContext

    override fun mount(configure: PlaywrightConfig.() -> Unit): PlaywrightContext {
        config.configure()

        val playwright = Playwright.create()
        val browser = config.browserType(playwright).launch(config.browserLaunchOptions)

        context = PlaywrightContext(playwright, browser)
        return context
    }

    override suspend fun beforeEach(testCase: TestCase) {
        val browserContext = context.browser.newContext(config.browserContextOptions)
        val page = browserContext.newPage()
        context.addTestContext(testCase, browserContext, page)
    }

    override suspend fun afterEach(
        testCase: TestCase,
        result: TestResult,
    ) {
        val testContext =
            context.getTestContext(testCase)
                ?: return
        if (result is TestResult.Failure) {
            val page = testContext.page

            if (config.savePageContentOnTestFailure) {
                page.savePageContent(config.pageContentsPath.resolve("${testCase.name.name}.$EXTENSION_PAGE_CONTENT"))
            }

            if (config.saveScreenshotOnTestFailure) {
                page.saveScreenshot(config.screenshotsPath.resolve("${testCase.name.name}.$EXTENSION_SCREENSHOT"))
            }
        }
        testContext.browserContext.close()
    }

    private fun Page.savePageContent(filePath: Path) {
        filePath.createParentDirectories()
        filePath.writeText(content())
        System.err.println("Page Content: ${filePath.toAbsolutePath().toUri()}")
    }

    private fun Page.saveScreenshot(filePath: Path) {
        filePath.createParentDirectories()
        screenshot(
            Page.ScreenshotOptions().apply {
                path = filePath
            },
        )
        System.err.println("Screenshot: ${filePath.toAbsolutePath().toUri()}")
    }

    override suspend fun afterSpec(spec: Spec) {
        context.playwright.close()
    }
}
