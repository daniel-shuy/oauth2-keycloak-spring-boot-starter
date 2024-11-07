package com.github.daniel.shuy.oauth2.keycloak.client.servlet

import com.github.daniel.shuy.oauth2.keycloak.client.KeycloakClientConfiguredCondition
import com.github.daniel.shuy.oauth2.keycloak.client.KeycloakOidcUserGrantedAuthoritiesConverter
import com.github.daniel.shuy.oauth2.keycloak.config.KeycloakWebSecurityConfigurerAdapter
import com.github.daniel.shuy.oauth2.keycloak.matcher.servlet.ResourceServerRequestMatcher
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.web.util.matcher.NegatedRequestMatcher

@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(OAuth2ClientAutoConfiguration::class)
@ConditionalOnClass(EnableWebSecurity::class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
internal class KeycloakServletClientConfiguration {
    @Bean
    fun keycloakOidcUserService(keycloakOidcUserGrantedAuthoritiesConverter: KeycloakOidcUserGrantedAuthoritiesConverter) =
        KeycloakOidcUserService(keycloakOidcUserGrantedAuthoritiesConverter)

    @Bean
    @ConditionalOnMissingBean
    @Conditional(KeycloakClientConfiguredCondition::class)
    fun keycloakOAuth2ClientConfigurer(
        clientRegistrationRepository: ClientRegistrationRepository,
        keycloakOidcUserService: KeycloakOidcUserService,
    ): KeycloakOAuth2ClientConfigurer =
        DefaultKeycloakOAuth2ClientConfigurer(
            clientRegistrationRepository,
            keycloakOidcUserService,
        )

    @Bean
    @ConditionalOnMissingBean
    @Conditional(KeycloakClientConfiguredCondition::class)
    fun keycloakOAuth2ClientSecurityFilterChain(
        http: HttpSecurity,
        resourceServerRequestMatcher: ResourceServerRequestMatcher?,
        keycloakWebSecurityConfigurerAdapter: KeycloakWebSecurityConfigurerAdapter,
        keycloakOAuth2ClientConfigurer: KeycloakOAuth2ClientConfigurer,
    ): KeycloakOAuth2ClientSecurityFilterChain {
        resourceServerRequestMatcher?.let { http.requestMatcher(NegatedRequestMatcher(it)) }
        keycloakWebSecurityConfigurerAdapter.configure(http)
        keycloakOAuth2ClientConfigurer.configureOAuth2Client(http)
        return KeycloakOAuth2ClientSecurityFilterChain(http)
    }
}
