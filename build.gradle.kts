import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "2.0.20"
    id("com.gradleup.shadow") version "8.3.3"
}

group = "io.github.theramu"
version = "2.2.0"

repositories {
    mavenCentral()
}

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "kotlin-kapt")
    apply(plugin = "com.gradleup.shadow")

    repositories {
        mavenCentral()
        maven("https://oss.sonatype.org/content/groups/public/")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
    }

    dependencies {
        implementation("io.github.theramu:dependency-loader:1.0.2")
        implementation("org.bstats:bstats-bukkit:3.0.3")
        implementation("org.bstats:bstats-bungeecord:3.0.3")
        implementation("org.bstats:bstats-velocity:3.0.3")
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        implementation("org.slf4j:slf4j-api:2.0.16")
        compileOnly("net.kyori:adventure-api:4.17.0")
        compileOnly("net.kyori:adventure-text-logger-slf4j:4.17.0")
        compileOnly("net.kyori:adventure-text-serializer-json:4.17.0")
        compileOnly("net.kyori:adventure-text-serializer-gson:4.17.0")
        compileOnly("net.kyori:adventure-text-serializer-legacy:4.17.0")
        compileOnly("net.kyori:adventure-text-minimessage:4.17.0")
    }

    tasks {
        compileJava {
            sourceCompatibility = "21"
            targetCompatibility = "21"
        }

        withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
            compilerOptions {
                jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
            }
        }

        processResources {
            filesMatching("*.yml") {
                expand(mapOf("project_version" to project.parent?.version))
            }
        }

        shadowJar {
            relocate("io.github.theramu.dependencyloader", "io.github.theramu.servershout.dependencyloader")
            relocate("org.bstats", "io.github.theramu.servershout.metrics")
        }
    }
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        from(subprojects.map {
            it.tasks.getByName<ShadowJar>("shadowJar").outputs
        })
        configurations = listOf()
    }

    jar {
        finalizedBy(shadowJar)
    }
}
