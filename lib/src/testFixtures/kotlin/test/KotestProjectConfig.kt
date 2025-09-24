package test

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.extensions.spring.SpringExtension

object KotestProjectConfig : AbstractProjectConfig() {
    override val extensions =
        listOf(
            SpringExtension(),
        )
}
