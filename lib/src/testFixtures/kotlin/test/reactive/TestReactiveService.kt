package test.reactive

import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Service

@Service
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
class TestReactiveService {
    suspend fun getPrincipalName(): String? =
        ReactiveSecurityContextHolder
            .getContext()
            .map { it.authentication }
            .map { it.name }
            .awaitFirstOrNull()
}
