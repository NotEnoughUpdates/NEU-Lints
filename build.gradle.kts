plugins {
    kotlin("jvm") version "1.8.22"
    `maven-publish`
}

group = "org.notenoughupdates.detektrules"
version = "1.0-SNAPSHOT"

repositories {
    maven("https://maven.minecraftforge.net/")
}
dependencies {
    compileOnly("io.gitlab.arturbosch.detekt:detekt-api:1.23.0")

    testImplementation("io.gitlab.arturbosch.detekt:detekt-test:1.23.0")
    testImplementation("io.kotest:kotest-assertions-core:5.6.2")
    testImplementation("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9:universal")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.3")
}

kotlin {
    jvmToolchain(8)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    systemProperty("junit.jupiter.testinstance.lifecycle.default", "per_class")
    systemProperty("compile-snippet-tests", project.hasProperty("compile-test-snippets"))
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
