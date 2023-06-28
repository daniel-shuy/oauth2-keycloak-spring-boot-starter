subprojects {
    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
