package test

import com.codeborne.selenide.Selenide
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import com.codeborne.selenide.Configuration as SelenideConfiguration

abstract class SelenideSpec(serverPort: Number) : StringSpec() {
    init {
        SelenideConfiguration.baseUrl = "http://localhost:$serverPort"
    }

    override suspend fun afterEach(
        testCase: TestCase,
        result: TestResult,
    ) {
        // don't reuse webdriver between tests to reset session
        Selenide.closeWebDriver()
    }
}
