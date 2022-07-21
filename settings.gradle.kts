pluginManagement {
    val kotlinVersion: String by settings
    val bootVersion: String by settings
    val springDependencyVersion: String by settings

    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("org.springframework.boot") version bootVersion
        id("io.spring.dependency-management") version springDependencyVersion
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.spring") version kotlinVersion
        kotlin("plugin.jpa") version kotlinVersion

    }
}

rootProject.name = "spark-spring"
