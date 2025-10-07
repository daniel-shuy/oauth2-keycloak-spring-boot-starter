package test.playwright

import com.microsoft.playwright.APIResponse
import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.APIResponseAssertions
import com.microsoft.playwright.assertions.LocatorAssertions
import com.microsoft.playwright.assertions.PageAssertions
import com.microsoft.playwright.assertions.PlaywrightAssertions
import io.kotest.core.extensions.install
import io.kotest.core.spec.Spec

object PlaywrightUtils {
    fun Spec.configurePlaywright(
        playwrightConfigurationProperties: PlaywrightConfigurationProperties,
        serverPort: Number,
    ): PlaywrightContext {
        val browserLaunchOptions =
            BrowserType.LaunchOptions().apply {
                headless = playwrightConfigurationProperties.headless
            }
        val browserContextOptions =
            Browser.NewContextOptions().apply {
                baseURL = "http://localhost:$serverPort"
            }
        return install(
            PlaywrightExtension(
                PlaywrightConfig(
                    browserLaunchOptions = browserLaunchOptions,
                    browserContextOptions = browserContextOptions,
                ),
            ),
        )
    }

    inline fun APIResponse.assert(assertion: APIResponseAssertions.() -> Unit) = PlaywrightAssertions.assertThat(this).assertion()

    inline fun Locator.assert(assertion: LocatorAssertions.() -> Unit) = PlaywrightAssertions.assertThat(this).assertion()

    inline fun Page.assert(assertion: PageAssertions.() -> Unit) = PlaywrightAssertions.assertThat(this).assertion()

    fun Page.submit() =
        locator("button[type='submit']")
            .click()
}
