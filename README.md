# oauth2-keycloak-spring-boot-starter

Spring Boot Starter for using Keycloak as the OAuth2 authorization server

| Version | Spring Boot |
|---------|-------------|
| 0.0.1   | 2.x.x       |

## Table of Contents

- [Configuration Properties](#configuration-properties)
- [Testing](#testing)

## Configuration Properties

| Configuration Property   | Mandatory/Optional | Description                                                                                                                                                    | Default |
|--------------------------|--------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------|---------|
| keycloak.enabled         | Optional           | Set to `false` to disable Spring Security integration with Keycloak.                                                                                           | `true`  |
| keycloak.auth-server-url | Mandatory          | The base URL of the Keycloak server. All other Keycloak pages and REST service endpoints are derived from this. It is usually of the form `https://host:port`. |         |
| keycloak.realm           | Mandatory          | Name of the realm.                                                                                                                                             |         |
| keycloak.client-id       | Mandatory          | The client-id of the application. Each application has a client-id that is used to identify the application.                                                   |         |
| keycloak.client-secret   | Optional           | Only for clients with `Confidential` access type. Specify the credentials of the application.                                                                  |         |
| keycloak.bearer-only     | Optional           | If enabled, will not attempt to authenticate users, but only verify bearer tokens.                                                                             | `true`  |

## Usage

Add the following to your Maven POM file:

```xml

<parent>
    <groupId>com.github.daniel.shuy</groupId>
    <artifactId>oauth2-keycloak-spring-boot-starter</artifactId>
    <version>${oauth2-keycloak-spring-boot-starter.version}</version>
</parent>
```

## Testing

Because `oauth2-keycloak-spring-boot-starter` sets the `spring-security-oauth2-client` and
`spring-security-oauth2-resource-server` configuration properties on environment initialization, when setting Keycloak
configuration properties for a test in an `ApplicationContextInitializer` using `TestPropertyValues`, the environment
needs to be processed again using `KeycloakOAuth2EnvironmentPostProcessor`, e.g.

```java

@SpringBootTest
@ContextConfiguration(initializers = KeycloakTest.Initializer.class)
class KeycloakTest {
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
