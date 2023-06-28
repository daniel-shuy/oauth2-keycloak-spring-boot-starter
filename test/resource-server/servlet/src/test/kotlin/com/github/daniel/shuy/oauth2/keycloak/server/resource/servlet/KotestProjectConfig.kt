package com.github.daniel.shuy.oauth2.keycloak.server.resource.servlet

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.extensions.spring.SpringExtension

object KotestProjectConfig : AbstractProjectConfig() {
    override fun extensions() = listOf(
        SpringExtension,
    )
}
