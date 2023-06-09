package com.github.daniel.shuy.oauth2.keycloak

import com.github.daniel.shuy.oauth2.keycloak.DefaultKeycloakJwtClaimsAuthoritiesConverter.Companion.CLAIM_REALM_ACCESS
import com.github.daniel.shuy.oauth2.keycloak.DefaultKeycloakJwtClaimsAuthoritiesConverter.Companion.CLAIM_RESOURCE_ACCESS
import org.springframework.core.convert.converter.Converter
import java.util.stream.Stream
import kotlin.streams.toList

typealias JwtClaims = Map<String, Any>

/**
 * Converts Keycloak bearer token to Spring Security authorities.
 */
interface KeycloakJwtClaimsAuthoritiesConverter : Converter<JwtClaims, Collection<String>>

/**
 * [KeycloakJwtClaimsAuthoritiesConverter] implementation for Keycloak that maps:
 * - Keycloak Realm Roles ([CLAIM_REALM_ACCESS] roles) -> prefixed with _ROLE
 * - Keycloak Client Roles ([CLAIM_RESOURCE_ACCESS] roles) -> unchanged
 */
open class DefaultKeycloakJwtClaimsAuthoritiesConverter(
    protected val keycloakResource: String,
) : KeycloakJwtClaimsAuthoritiesConverter {
    companion object {
        protected const val CLAIM_REALM_ACCESS = "realm_access"
        protected const val CLAIM_RESOURCE_ACCESS = "resource_access"

        protected const val KEY_ROLES = "roles"
        protected const val GRANTED_AUTHORITY_PREFIX_ROLE = "ROLE"

        protected fun rolesFromAccess(access: Map<*, *>): Collection<*> =
            access[KEY_ROLES] as? Collection<*> ?: emptyList<Any>()
    }

    override fun convert(jwtClaims: JwtClaims): Collection<String> =
        Stream.concat(convertRealmRoles(jwtClaims), convertResourceRoles(jwtClaims))
            .toList()

    protected fun convertRealmRoles(jwtClaims: JwtClaims): Stream<String> {
        val realmAccess = jwtClaims[CLAIM_REALM_ACCESS] as? Map<*, *>
            ?: return Stream.empty()
        val roles = rolesFromAccess(realmAccess)
        return roles.stream()
            .map { role -> "${GRANTED_AUTHORITY_PREFIX_ROLE}_$role" }
    }

    protected fun convertResourceRoles(jwtClaims: JwtClaims): Stream<String> {
        val accessByResource = jwtClaims[CLAIM_RESOURCE_ACCESS] as? Map<*, *>
            ?: return Stream.empty()
        val access = accessByResource[keycloakResource] as? Map<*, *>
            ?: return Stream.empty()
        val roles = rolesFromAccess(access)
        return roles.stream()
            .map { it.toString() }
    }
}
