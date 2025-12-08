plugins {
    kotlin("jvm") version "2.2.21"
    `java-library`
}

group = "org.dongchyeon"
version = "0.0.1-SNAPSHOT"

dependencies {
    api(platform("org.springframework.boot:spring-boot-dependencies:4.0.0"))
    api("org.springframework:spring-context")
    api("org.jetbrains.kotlin:kotlin-reflect")
    api("com.google.code.gson:gson:2.11.0")
    api("com.fasterxml.jackson.core:jackson-annotations")
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)
}
