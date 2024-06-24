# Formatting

To check if the code is properly formatted, run:

```shell
./gradlew ktlintCheck
```

To automatically fix any formatting issues, run:

```shell
./gradlew ktlintFormat
```

If using IntelliJ IDEA or Android Studio, install the [Ktlint plugin](https://plugins.jetbrains.com/plugin/15057-ktlint)
and configure the IDE to format Kotlin source files using `ktlint` by setting the mode to **Distract Free** or
**Manual** in **Tools -> KtLint -> Mode**.

