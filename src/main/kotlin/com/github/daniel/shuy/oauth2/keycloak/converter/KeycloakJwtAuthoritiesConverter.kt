package com.github.daniel.shuy.oauth2.keycloak.converter

import com.github.daniel.shuy.oauth2.keycloak.converter.DefaultKeycloakJwtAuthoritiesConverter.Companion.CLAIM_REALM_ACCESS
import com.github.daniel.shuy.oauth2.keycloak.converter.DefaultKeycloakJwtAuthoritiesConverter.Companion.CLAIM_RESOURCE_ACCESS
import org.springframework.core.convert.converter.Converter
import org.springframework.security.oauth2.jwt.Jwt
import java.util.stream.Stream
import kotlin.streams.toList

/**
 * Converts Keycloak bearer token to Spring Security authorities.
 */
interface KeycloakJwtAuthoritiesConverter : Converter<Jwt, Collection<String>>

/**
 * [KeycloakJwtAuthoritiesConverter] implementation for Keycloak that maps:
 * - Keycloak Realm Roles ([CLAIM_REALM_ACCESS] roles) -> prefixed with _ROLE
 * - Keycloak Client Roles ([CLAIM_RESOURCE_ACCESS] roles) -> unchanged
 */
open class DefaultKeycloakJwtAuthoritiesConverter(
    protected val keycloakResource: String,
) : KeycloakJwtAuthoritiesConverter {
    companion object {
        protected const val CLAIM_REALM_ACCESS = "realm_access"
        protected const val CLAIM_RESOURCE_ACCESS = "resource_access"

        protected const val KEY_ROLES = "roles"
        protected const val GRANTED_AUTHORITY_PREFIX_ROLE = "ROLE"

        protected fun rolesFromAccess(access: Map<*, *>): Collection<*> =
            access[KEY_ROLES] as? Collection<*> ?: emptyList<Any>()
    }

    override fun convert(jwt: Jwt): Collection<String> =
        Stream.concat(convertRealmRoles(jwt), convertResourceRoles(jwt))
            .toList()

    protected fun convertRealmRoles(jwt: Jwt): Stream<String> {
        val realmAccess = jwt.getClaim(CLAIM_REALM_ACCESS) as? Map<*, *>
            ?: return Stream.empty()
        val roles = rolesFromAccess(realmAccess)
        return roles.stream()
            .map { role -> "${GRANTED_AUTHORITY_PREFIX_ROLE}_$role" }
    }

    protected fun convertResourceRoles(jwt: Jwt): Stream<String> {
        val accessByResource = jwt.getClaim(CLAIM_RESOURCE_ACCESS) as? Map<*, *>
            ?: return Stream.empty()
        val access = accessByResource[keycloakResource] as? Map<*, *>
            ?: return Stream.empty()
        val roles = rolesFromAccess(access)
        return roles.stream()
            .map { it.toString() }
    }
}
