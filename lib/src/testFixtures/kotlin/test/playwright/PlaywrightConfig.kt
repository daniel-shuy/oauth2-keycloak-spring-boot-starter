package test.playwright

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Playwright
import java.nio.file.Path

data class PlaywrightConfig(
    val savePageContentOnTestFailure: Boolean = true,
    val saveScreenshotOnTestFailure: Boolean = true,
    val pageContentsPath: Path = Path.of("build", "reports", "tests"),
    val screenshotsPath: Path = Path.of("build", "reports", "tests"),
    val browserType: Playwright.() -> BrowserType = { chromium() },
    val browserLaunchOptions: BrowserType.LaunchOptions = BrowserType.LaunchOptions(),
    val browserContextOptions: Browser.NewContextOptions = Browser.NewContextOptions(),
)
