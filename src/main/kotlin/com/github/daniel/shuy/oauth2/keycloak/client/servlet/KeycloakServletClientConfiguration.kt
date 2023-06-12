package com.github.daniel.shuy.oauth2.keycloak.client.servlet

import com.github.daniel.shuy.oauth2.keycloak.KeycloakJwtClaimsAuthoritiesConverter
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.security.oauth2.client.ClientsConfiguredCondition
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository

@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(OAuth2ClientAutoConfiguration::class)
@ConditionalOnClass(EnableWebSecurity::class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
internal class KeycloakServletClientConfiguration {
    @Bean
    @ConditionalOnMissingBean
    fun keycloakOidcUserGrantedAuthoritiesConverter(
        keycloakJwtClaimsAuthoritiesConverter: KeycloakJwtClaimsAuthoritiesConverter,
    ): KeycloakOidcUserGrantedAuthoritiesConverter = DefaultKeycloakOidcUserGrantedAuthoritiesConverter(
        keycloakJwtClaimsAuthoritiesConverter,
    )

    @Bean
    fun keycloakOidcUserService(
        keycloakOidcUserGrantedAuthoritiesConverter: KeycloakOidcUserGrantedAuthoritiesConverter,
    ) = KeycloakOidcUserService(keycloakOidcUserGrantedAuthoritiesConverter)

    @Bean
    @ConditionalOnMissingBean
    @Conditional(ClientsConfiguredCondition::class) // somehow @ConditionalOnBean(ClientRegistrationRepository::class) doesn't work
    fun keycloakOAuth2ClientConfigurer(
        clientRegistrationRepository: ClientRegistrationRepository,
        keycloakOidcUserService: KeycloakOidcUserService,
    ): KeycloakOAuth2ClientConfigurer = DefaultKeycloakOAuth2ClientConfigurer(
        clientRegistrationRepository,
        keycloakOidcUserService,
    )
}
