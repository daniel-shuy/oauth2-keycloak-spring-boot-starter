subprojects {
    // configure the test task to run Kotest instead of JUnit
    tasks.withType<Test>().configureEach {
        if (name == "test") {
            enabled = false
            tasks
                .findByName("kotest")
                ?.let { kotestTask -> dependsOn(kotestTask) }
        }
    }
}
