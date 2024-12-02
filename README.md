# oauth2-keycloak-spring-boot-starter

Spring Boot Starter for using Keycloak as the OAuth2 authorization server

| Version | Spring Boot |
|---------|-------------|
| 1.x.x   | 2.x.x       |

## Table of Contents

- [Configuration Properties](#configuration-properties)
- [Usage](#usage)
  - [OAuth2 Client](#oauth2-client)
  - [OAuth2 Resource Server](#oauth2-resource-server)
  - [OAuth2 Client and Resource Server](#oauth2-client-and-resource-server)
  - [Configure `HttpSecurity` (Spring MVC)](#configure-httpsecurity-spring-mvc)
  - [Configure `ServerHttpSecurity` (Spring WebFlux)](#configure-serverhttpsecurity-spring-webflux)
- [CSRF Protection](#csrf-protection)
  - [Spring MVC](#spring-mvc)
  - [Spring WebFlux](#spring-webflux)
- [Testing](#testing)

## Configuration Properties

| Configuration Property                                   | Mandatory/Optional | Description                                                                                                                                                | Default  |
| -------------------------------------------------------- | ------------------ |------------------------------------------------------------------------------------------------------------------------------------------------------------| -------- |
| keycloak.enabled                                         | Optional           | Set to `false` to disable Spring Security integration with Keycloak.                                                                                       | `true`   |
| keycloak.auth-server-url                                 | Mandatory          | Base URL of the Keycloak server. All other Keycloak pages and REST service endpoints are derived from this. It is usually of the form `https://host:port`. |          |
| keycloak.realm                                           | Mandatory          | Name of the realm.                                                                                                                                         |          |
| keycloak.client-id                                       | Mandatory          | Client-id of the application. Each application has a client-id that is used to identify the application.                                                   |          |
| keycloak.client-secret                                   | Optional           | Only for clients with `Confidential` access type. Specify the credentials of the application.                                                              |          |
| keycloak.bearer-only                                     | Optional           | If enabled, will not attempt to authenticate users, but only verify bearer tokens.                                                                         | `true`   |
| keycloak.principal-attribute                             | Optional           | Token claim attribute to obtain as principal name.                                                                                                         | `sub`    |
| keycloak.spring-security-oauth2-client-provider-name     | Optional           | Name of the Spring Security OAuth2 Client Provider to register.                                                                                            | keycloak |
| keycloak.spring-security-oauth2-client-registration-name | Optional           | Name of the Spring Security OAuth2 Client Registration to register.                                                                                        | keycloak |

## Usage

Maven:

```xml
<!-- pom.xml -->
<dependency>
    <groupId>com.github.daniel.shuy</groupId>
    <artifactId>oauth2-keycloak-spring-boot-starter</artifactId>
    <version>${oauth2-keycloak-spring-boot-starter.version}</version>
</dependency>
```

Gradle:

```groovy
// build.gradle
implementation "com.github.daniel.shuy:oauth2-keycloak-spring-boot-starter:${oauth2KeycloakSpringBootStarterVersion}"
```

Gradle (Kotlin):

```kotlin
// build.gradle.kts
implementation("com.github.daniel.shuy:oauth2-keycloak-spring-boot-starter:${oauth2KeycloakSpringBootStarterVersion}")
```

The service can be configured as either an OAuth2 client, an OAuth2 resource server, or both:

- **Client:**
  - Redirects to interactive login if unauthenticated
  - Stores access token in session
  - Usually used for client applications (e.g. Thymeleaf)
- **Resource Server:** Validates bearer tokens

### OAuth2 Client

Maven:

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>
```

Gradle:

```groovy
// build.gradle
implementation 'org.springframework.boot:spring-boot-starter-oauth2-client'
```

Gradle (Kotlin):

```kotlin
// build.gradle.kts
implementation("org.springframework.boot:ospring-boot-starter-oauth2-client")
```

### OAuth2 Resource Server

Maven:

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

Gradle:

```groovy
// build.gradle
implementation 'org.springframework.boot:spring-boot-starter-oauth2-resource-server'
```

Gradle (Kotlin):

```kotlin
// build.gradle.kts
implementation("org.springframework.boot:ospring-boot-starter-oauth2-resource-server")
```

**IMPORTANT: Do not configure any other OAuth2 Resource Server, as Spring Security OAuth2 Resource Server only supports
configuring 1 resource server.**

### OAuth2 Client and Resource Server

Maven:

```xml
<!-- pom.xml -->
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-client</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
    </dependency>
</dependencies>
```

Gradle:

```groovy
// build.gradle
implementation 'org.springframework.boot:spring-boot-starter-oauth2-client'
implementation 'org.springframework.boot:spring-boot-starter-oauth2-resource-server'
```

Gradle (Kotlin):

```kotlin
// build.gradle.kts
implementation("org.springframework.boot:ospring-boot-starter-oauth2-client")
implementation("org.springframework.boot:ospring-boot-starter-oauth2-resource-server")
```

**IMPORTANT: Do not configure any other OAuth2 Resource Server, as Spring Security OAuth2 Resource Server only supports
configuring 1 resource server.**

### Configure `HttpSecurity` (Spring MVC)

The `SecurityFilterChain`(s) will be created automatically.
Create a `KeycloakHttpSecurityCustomizer` to configure the `HttpSecurity`.

Minimal example:

```java

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
  @Bean
  public KeycloakHttpSecurityCustomizer keycloakHttpSecurityCustomizer() {
    return http -> {
      http.authorizeRequests(authorize ->
          authorize
              .anyRequest()
              .authenticated()
      );
    };
  }
}
```

**IMPORTANT: Do not override the `requestMatcher(RequestMatcher)` of the `HttpSecurity`.
The filter(s) must be applied to the root path.**

### Configure `ServerHttpSecurity (Spring WebFlux)

The `SecurityWebFilterChain`(s) will be created automatically.
Create a `KeycloakServerHttpSecurityCustomizer` to configure the `ServerHttpSecurity`.

Minimal example:

```java

@Configuration
@EnableWebFluxSecurity
public class WebSecurityConfig {
  @Bean
  public KeycloakServerHttpSecurityCustomizer keycloakServerHttpSecurityCustomizer() {
    return http -> {
      http.authorizeExchange(exchanges ->
          exchanges
              .anyExchange()
              .authenticated()
      );
    };
  }
}
```

**IMPORTANT: Do not override the `securityMatcher(ServerWebExchangeMatcher)` of the `ServerHttpSecurity`.
The filter(s) must be applied to the root path.**

## CSRF Protection

If the protected resources are stateless, the CSRF protection can be disabled, e.g.

### Spring MVC

```java

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
  @Bean
  public KeycloakHttpSecurityCustomizer keycloakHttpSecurityCustomizer() {
    return http -> {
      http
          .csrf(csrf ->
              csrf.disable()
          );
          // ...
    };
  }
}
```

### Spring WebFlux

```java

@Configuration
@EnableWebFluxSecurity
public class WebSecurityConfig {
  @Bean
  public KeycloakServerHttpSecurityCustomizer keycloakServerHttpSecurityCustomizer() {
    return http -> {
      http
          .csrf(csrf ->
              csrf.disable()
          );
          // ...
    };
  }
}
```

## Testing

Because `oauth2-keycloak-spring-boot-starter` sets the `spring-security-oauth2-client` and
`spring-security-oauth2-resource-server` configuration properties on environment initialization, when setting Keycloak
configuration properties for a test in an `ApplicationContextInitializer` using `TestPropertyValues`, the required
configuration properties must first be configured with dummy values, and the environment needs to be processed again
using `KeycloakOAuth2EnvironmentPostProcessor`, e.g.

```java

@SpringBootTest(properties = {
    "keycloak.auth-server-url=<placeholder>",
    "keycloak.realm=" + KeycloakTest.KEYCLOAK_REALM,
    "keycloak.client-id=" + KeycloakTest.KEYCLOAK_CLIENT_ID,
})
@ContextConfiguration(initializers = KeycloakTest.Initializer.class)
class KeycloakTest {
  protected static final String KEYCLOAK_REALM = "realm";
  protected static final String KEYCLOAK_CLIENT_ID = "client";

  static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
      var applicationEnvironment = applicationContext.getEnvironment();
      TestPropertyValues.of("keycloak.auth-server-url=" + keycloakAuthServerUrl).applyTo(applicationEnvironment);
      KeycloakOAuth2EnvironmentPostProcessor.postProcessEnvironment(applicationEnvironment);
    }
  }
}
```
