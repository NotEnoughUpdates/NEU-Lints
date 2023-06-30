import java.io.ByteArrayOutputStream

plugins {
    kotlin("jvm") version "1.8.22"
    `maven-publish`
}

group = "org.notenoughupdates"

fun cmd(vararg args: String): String? {
    val output = ByteArrayOutputStream()
    val r = exec {
        this.commandLine(args.toList())
        this.isIgnoreExitValue = true
        this.standardOutput = output
        this.errorOutput = ByteArrayOutputStream()
    }
    return if (r.exitValue == 0) output.toByteArray().decodeToString().trim()
    else null
}

val tag = cmd("git", "describe", "--tags", "HEAD")
val hash = cmd("git", "rev-parse", "--short", "HEAD")!!
val isSnapshot = tag == null
version = tag ?: hash

repositories {
    maven("https://maven.minecraftforge.net/")
}
dependencies {
    compileOnly("io.gitlab.arturbosch.detekt:detekt-api:1.23.0")

    testImplementation("io.gitlab.arturbosch.detekt:detekt-test:1.23.0")
    testImplementation("io.kotest:kotest-assertions-core:5.6.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.3")
}

kotlin {
    jvmToolchain(8)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    systemProperty("junit.jupiter.testinstance.lifecycle.default", "per_class")
    systemProperty("compile-snippet-tests", project.hasProperty("compile-test-snippets"))
    fun getDep(name: String) = project(":minecraft").configurations.compileClasspath.get()
        .files { it.name == name }.filter { "natives" !in it.name && name in it.name }.single()
    systemProperty("dependency.minecraft", getDep("minecraft-mapped"))
    systemProperty("dependency.forge", getDep("forge-mapped"))
    systemProperty("dependency.lwjgl", getDep("lwjgl"))
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                licenses {
                    license {
                        name.set("Apache-2.0")
                        url.set("https://github.com/NotEnoughUpdates/NEU-Lints/blob/HEAD/LICENSE")
                    }
                }
                developers {
                    developer {
                        name.set("Linnea Gräf")
                    }
                }
                scm {
                    url.set("https://github.com/NotEnoughUpdates/NEU-Lints")
                }
            }

        }
    }
    repositories {
        if (project.hasProperty("neulintsPassword")) {
            maven {
                url = uri("https://maven.notenoughupdates.org/releases")
                name = "neulints"
                credentials(PasswordCredentials::class)
                authentication {
                    create<BasicAuthentication>("basic")
                }
            }
        }
    }
}
