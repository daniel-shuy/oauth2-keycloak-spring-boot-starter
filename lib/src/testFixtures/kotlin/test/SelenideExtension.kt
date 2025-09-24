package test

import com.codeborne.selenide.Selenide
import io.kotest.core.listeners.AfterEachListener
import io.kotest.core.listeners.BeforeSpecListener
import io.kotest.core.spec.Spec
import io.kotest.core.test.TestCase
import io.kotest.engine.test.TestResult
import com.codeborne.selenide.Configuration as SelenideConfiguration

class SelenideExtension(private val serverPort: Number) : BeforeSpecListener, AfterEachListener {
    override suspend fun beforeSpec(spec: Spec) {
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
