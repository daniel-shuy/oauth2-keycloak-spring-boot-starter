package com.github.daniel.shuy.oauth2.keycloak.client.reactive

import com.github.daniel.shuy.oauth2.keycloak.client.KeycloakClientConfiguredCondition
import com.github.daniel.shuy.oauth2.keycloak.client.KeycloakOidcUserGrantedAuthoritiesConverter
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.security.oauth2.client.reactive.ReactiveOAuth2ClientAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import reactor.core.publisher.Flux

@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(ReactiveOAuth2ClientAutoConfiguration::class)
@ConditionalOnClass(
    Flux::class,
    EnableWebFluxSecurity::class,
)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
internal class KeycloakReactiveClientConfiguration {
    @Bean
    @ConditionalOnMissingBean
    @Conditional(KeycloakClientConfiguredCondition::class)
    fun keycloakReactiveOAuth2ClientConfigurer(
        clientRegistrationRepository: ReactiveClientRegistrationRepository,
    ): KeycloakReactiveOAuth2ClientConfigurer = DefaultKeycloakReactiveOAuth2ClientConfigurer(
        clientRegistrationRepository,
    )

    @Bean
    fun keycloakOidcReactiveOAuth2UserService(
        keycloakOidcUserGrantedAuthoritiesConverter: KeycloakOidcUserGrantedAuthoritiesConverter,
    ) = KeycloakOidcReactiveOAuth2UserService(keycloakOidcUserGrantedAuthoritiesConverter)
}
