package test.servlet

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
class TestService {
    fun getPrincipalName(): String? = SecurityContextHolder.getContext().authentication?.name
}
