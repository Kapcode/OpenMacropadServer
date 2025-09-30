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
    // Direct JAR dependency to KotlinNetworkLibrary
    implementation(files("../KotlinNetworkLibrary/build/libs/KotlinNetworkLibrary-1.0.0.jar"))

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
sourceSets {
    main {
        java.srcDirs("src")
    }
}