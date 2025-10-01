plugins {
    kotlin("jvm") version "2.2.0"
    application
}

group = "com.kapcode"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // JAR from project's libs folder
    implementation(files("libs/KotlinNetworkLibrary-1.0.0.jar"))

    testImplementation(kotlin("test"))
}

application {
    mainClass.set("MainKt")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(24)
}