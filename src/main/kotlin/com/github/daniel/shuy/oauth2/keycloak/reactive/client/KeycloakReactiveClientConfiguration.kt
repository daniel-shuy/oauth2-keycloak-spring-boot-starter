package com.github.daniel.shuy.oauth2.keycloak.reactive.client

import com.github.daniel.shuy.oauth2.keycloak.converter.KeycloakJwtAuthoritiesConverter
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.security.oauth2.client.ClientsConfiguredCondition
import org.springframework.boot.autoconfigure.security.oauth2.client.reactive.ReactiveOAuth2ClientAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder

@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(ReactiveOAuth2ClientAutoConfiguration::class)
@ConditionalOnClass(ClientRegistration::class)
internal class KeycloakReactiveClientConfiguration {
    @Bean
    @ConditionalOnMissingBean
    @Conditional(ClientsConfiguredCondition::class) // somehow @ConditionalOnBean(ReactiveClientRegistrationRepository::class) doesn't work
    fun keycloakReactiveOAuth2ClientConfigurer(
        clientRegistrationRepository: ReactiveClientRegistrationRepository,
    ): KeycloakReactiveOAuth2ClientConfigurer = DefaultKeycloakReactiveOAuth2ClientConfigurer(
        clientRegistrationRepository,
    )

    @Bean
    @ConditionalOnMissingBean
    fun keycloakReactiveOidcUserGrantedAuthoritiesConverter(
        jwtDecoder: ReactiveJwtDecoder,
        keycloakJwtAuthoritiesConverter: KeycloakJwtAuthoritiesConverter,
    ): KeycloakReactiveOidcUserGrantedAuthoritiesConverter =
        DefaultKeycloakReactiveOidcUserGrantedAuthoritiesConverter(
            jwtDecoder,
            keycloakJwtAuthoritiesConverter,
        )

    @Bean
    fun keycloakOidcReactiveOAuth2UserService(
        keycloakReactiveOidcUserGrantedAuthoritiesConverter: KeycloakReactiveOidcUserGrantedAuthoritiesConverter,
    ) = KeycloakOidcReactiveOAuth2UserService(keycloakReactiveOidcUserGrantedAuthoritiesConverter)
}
