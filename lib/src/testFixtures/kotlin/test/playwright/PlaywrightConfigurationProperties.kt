package test.playwright

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("playwright")
data class PlaywrightConfigurationProperties(
    val headless: Boolean = true,
)
