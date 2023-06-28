# oauth2-keycloak-spring-boot-starter

Spring Boot Starter for using Keycloak as the OAuth2 authorization server

| Version | Spring Boot |
| ------- | ----------- |
| 0.0.1   | 2.x.x       |

## Table of Contents

- [Configuration Properties](#configuration-properties)
- [Usage](#usage)
  - [OAuth2 Client](#oauth2-client)
    - [Spring MVC](#spring-mvc)
    - [Spring WebFlux](#spring-webflux)
  - [OAuth2 Resource Server](#oauth2-resource-server)
    - [Spring MVC](#spring-mvc-1)
    - [Spring WebFlux](#spring-webflux-1)
    - [Multiple Resource Servers](#multiple-resource-servers)
      - [Spring MVC](#spring-mvc-2)
      - [Spring WebFlux](#spring-webflux-2)
  - [OAuth2 Client and Resource Server](#oauth2-client-and-resource-server)
    - [Spring MVC](#spring-mvc-3)
    - [Spring WebFlux](#spring-webflux-3)
- [Testing](#testing)

## Configuration Properties

| Configuration Property                                   | Mandatory/Optional | Description                                                                                                                                                    | Default  |
| -------------------------------------------------------- | ------------------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------- |
| keycloak.enabled                                         | Optional           | Set to `false` to disable Spring Security integration with Keycloak.                                                                                           | `true`   |
| keycloak.auth-server-url                                 | Mandatory          | The base URL of the Keycloak server. All other Keycloak pages and REST service endpoints are derived from this. It is usually of the form `https://host:port`. |          |
| keycloak.realm                                           | Mandatory          | Name of the realm.                                                                                                                                             |          |
| keycloak.client-id                                       | Mandatory          | The client-id of the application. Each application has a client-id that is used to identify the application.                                                   |          |
| keycloak.client-secret                                   | Optional           | Only for clients with `Confidential` access type. Specify the credentials of the application.                                                                  |          |
| keycloak.bearer-only                                     | Optional           | If enabled, will not attempt to authenticate users, but only verify bearer tokens.                                                                             | `true`   |
| keycloak.spring-security-oauth2-client-provider-name     | Optional           | The name of the Spring Security OAuth2 Client Provider to register.                                                                                            | keycloak |
| keycloak.spring-security-oauth2-client-registration-name | Optional           | The name of the Spring Security OAuth2 Client Registration to register.                                                                                        | keycloak |

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

## OAuth2 Client

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

Use `KeycloakWebSecurityConfigurer` to configure a filter as an OAuth2 client.

Minimal example:

### Spring MVC

```java

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
  @Bean
  public SecurityFilterChain keycloakClientFilterChain(HttpSecurity http,
                                                       KeycloakWebSecurityConfigurer keycloakWebSecurityConfigurer)
      throws Exception {
    keycloakWebSecurityConfigurer.configureOAuth2Client(http);

    http.authorizeRequests(authorize -> authorize
        .anyRequest()
        .authenticated()
    );

    return http.build();
  }
}
```

**IMPORTANT: Do not override the `requestMatcher(RequestMatcher)` of the `HttpSecurity`.
The OAuth2 Client filter must be applied to the root path.**

### Spring WebFlux

```java

@Configuration
@EnableWebFluxSecurity
public class WebSecurityConfig {
  @Bean
  public SecurityWebFilterChain keycloakClientFilterChain(ServerHttpSecurity http,
                                                          KeycloakWebSecurityConfigurer keycloakWebSecurityConfigurer)
      throws Exception {
    keycloakWebSecurityConfigurer.configureOAuth2Client(http);

    http.authorizeExchange(exchanges -> exchanges
        .anyExchange()
        .authenticated()
    );

    return http.build();
  }
}
```

**IMPORTANT: Do not override the `securityMatcher(ServerWebExchangeMatcher)` of the `ServerHttpSecurity`.
The OAuth2 Client filter must be applied to the root path.**

## OAuth2 Resource Server

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

Use `KeycloakWebSecurityConfigurer` to configure a filter as an OAuth2 resource server.

Minimal example:

### Spring MVC

```java

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
  @Bean
  public SecurityFilterChain keycloakResourceServerFilterChain(
      HttpSecurity http, KeycloakWebSecurityConfigurer keycloakWebSecurityConfigurer) throws Exception {
    keycloakWebSecurityConfigurer.configureOAuth2ResourceServer(http);

    http.authorizeRequests(authorize -> authorize
        .anyRequest()
        .authenticated()
    );

    return http.build();
  }
}
```

### Spring WebFlux

```java

@Configuration
@EnableWebFluxSecurity
public class WebSecurityConfig {
  @Bean
  public SecurityWebFilterChain keycloakResourceServerFilterChain(
      ServerHttpSecurity http, KeycloakWebSecurityConfigurer keycloakWebSecurityConfigurer) throws Exception {
    keycloakWebSecurityConfigurer.configureOAuth2ResourceServer(http);

    http.authorizeExchange(exchanges -> exchanges
        .anyExchange()
        .authenticated()
    );

    return http.build();
  }
}
```

### Multiple resource servers

To restrict a Keycloak resource server filter to a subset of requests, simply configure a matcher for the filter, e.g.

#### Spring MVC

```java

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
  @Bean
  public SecurityFilterChain keycloakResourceServerFilterChain(
          HttpSecurity http,
          KeycloakWebSecurityConfigurer keycloakWebSecurityConfigurer,
          HandlerMappingIntrospector introspector) throws Exception {
    http.requestMatcher(new MvcRequestMatcher(introspector, "/api/**"));

    keycloakWebSecurityConfigurer.configureOAuth2ResourceServer(http);

    http.authorizeRequests(authorize -> authorize
            .anyRequest()
            .authenticated()
    );

    return http.build();
  }
}
```

**IMPORTANT: `HttpSecurity#requestMatcher(RequestMatcher)` should be called before
`KeycloakWebSecurityConfigurer#configureOAuth2ResourceServer(HttpSecurity)`.**

#### Spring WebFlux

```java

@Configuration
@EnableWebFluxSecurity
public class WebSecurityConfig {
  @Bean
  public SecurityWebFilterChain keycloakResourceServerFilterChain(
          ServerHttpSecurity http, KeycloakWebSecurityConfigurer keycloakWebSecurityConfigurer) throws Exception {
    http.securityMatcher(new PathPatternParserServerWebExchangeMatcher("/api/**"));

    keycloakWebSecurityConfigurer.configureOAuth2ResourceServer(http);

    http.authorizeExchange(exchanges -> exchanges
            .anyExchange()
            .authenticated()
    );

    return http.build();
  }
}
```

**IMPORTANT: `ServerHttpSecurity#securityMatcher(ServerWebExchangeMatcher)` should be called before
`KeycloakWebSecurityConfigurer#configureOAuth2ResourceServer(ServerHttpSecurity)`.**

## OAuth2 Client and Resource Server

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

Use `KeycloakWebSecurityConfigurer` to configure a filter as an OAuth2 client, and another filter as an OAuth2 resource
server.

**IMPORTANT: The OAuth2 client filter must be defined before the resource server filter.**

The common configuration between both filters can be separated out into another function.

Minimal example:

### Spring MVC

```java

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
  @Bean
  public SecurityWebFilterChain keycloakClientFilterChain(ServerHttpSecurity http,
                                                          KeycloakWebSecurityConfigurer keycloakWebSecurityConfigurer)
      throws Exception {
    keycloakWebSecurityConfigurer.configureOAuth2Client(http);
    configureWebSecurity(http);
    return http.build();
  }

  @Bean
  public SecurityFilterChain keycloakResourceServerFilterChain(
      HttpSecurity http, KeycloakWebSecurityConfigurer keycloakWebSecurityConfigurer) throws Exception {
    keycloakWebSecurityConfigurer.configureOAuth2ResourceServer(http);
    configureWebSecurity(http);
    return http.build();
  }

  private void configureWebSecurity(HttpSecurity http) {
    http.authorizeRequests(authorize -> authorize
        .anyRequest()
        .authenticated()
    );
  }
}
```

### Spring WebFlux

```java

@Configuration
@EnableWebFluxSecurity
public class WebSecurityConfig {
  @Bean
  public SecurityWebFilterChain keycloakClientFilterChain(ServerHttpSecurity http,
                                                          KeycloakWebSecurityConfigurer keycloakWebSecurityConfigurer)
      throws Exception {
    keycloakWebSecurityConfigurer.configureOAuth2Client(http);
    configureWebSecurity(http);
    return http.build();
  }

  @Bean
  public SecurityWebFilterChain keycloakResourceServerFilterChain(
      ServerHttpSecurity http, KeycloakWebSecurityConfigurer keycloakWebSecurityConfigurer) throws Exception {
    keycloakWebSecurityConfigurer.configureOAuth2ResourceServer(http);
    configureWebSecurity(http);
    return http.build();
  }

  private void configureWebSecurity(ServerHttpSecurity http) {
    http.authorizeExchange(exchanges -> exchanges
        .anyExchange()
        .authenticated()
    );
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
