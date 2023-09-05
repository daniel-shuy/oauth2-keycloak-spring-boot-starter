package test

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(PrefixedController.REQUEST_MAPPING)
class PrefixedController : TestController() {
    companion object {
        const val REQUEST_MAPPING = "/prefix"
    }
}
