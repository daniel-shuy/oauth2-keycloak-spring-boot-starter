package test.reactive

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
class TestReactiveController(
    private val testReactiveService: TestReactiveService,
) {
    companion object {
        const val REQUEST_MAPPING_PATH_HELLO_WORLD = "/hello-world"
        const val REQUEST_MAPPING_PATH_PRINCIPAL_NAME = "/principal-name"
        const val REQUEST_MAPPING_PATH_FOO = "/foo"
        const val REQUEST_MAPPING_PATH_BAR = "/bar"
        const val REQUEST_MAPPING_PATH_FAIL_1 = "/fail1"
        const val REQUEST_MAPPING_PATH_FAIL_2 = "/fail2"

        const val RESPONSE_BODY_HELLO_WORLD = "Hello, World!"
        const val RESPONSE_BODY_FOO = "foo"
        const val RESPONSE_BODY_BAR = "bar"
        const val RESPONSE_BODY_FAIL = "You should not be seeing this!"
    }

    @GetMapping(REQUEST_MAPPING_PATH_HELLO_WORLD)
    fun helloWorld() = RESPONSE_BODY_HELLO_WORLD

    @GetMapping(REQUEST_MAPPING_PATH_PRINCIPAL_NAME)
    suspend fun principalAttribute() = testReactiveService.getPrincipalName()

    @GetMapping(REQUEST_MAPPING_PATH_FOO)
    fun foo() = RESPONSE_BODY_FOO

    @GetMapping(REQUEST_MAPPING_PATH_BAR)
    fun bar() = RESPONSE_BODY_BAR

    @GetMapping(REQUEST_MAPPING_PATH_FAIL_1)
    fun fail1() = RESPONSE_BODY_FAIL

    @GetMapping(REQUEST_MAPPING_PATH_FAIL_2)
    fun fail2() = RESPONSE_BODY_FAIL
}
