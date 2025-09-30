plugins {
    kotlin("jvm") version "2.2.0"
    application
}

group = "com.kapcode"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal() // For local development
    mavenCentral()
    maven { url = uri("https://jitpack.io") } // For GitHub releases
}

dependencies {
    // Your network library (using mavenLocal for now)
    implementation("com.kapcode:kotlin-network-library:1.0.0")

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
