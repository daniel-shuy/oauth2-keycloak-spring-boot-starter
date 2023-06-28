package com.github.daniel.shuy.oauth2.keycloak.client.reactive

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController {
    @GetMapping("/hello-world")
    fun helloWorld(): String = "Hello, World!"
}
