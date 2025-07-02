rootProject.name = "pridge"

pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
        }
        gradlePluginPortal()
    }

    val loom_version: String by settings
    val kotlin_version: String by settings

    plugins {
        id("fabric-loom") version loom_version
        id("org.jetbrains.kotlin.jvm") version kotlin_version
    }
}