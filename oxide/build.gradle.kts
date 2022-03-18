plugins {
    kotlin("js") version "1.6.10"
}

group = "me.cubuspl42"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

kotlin {
    js(LEGACY) {
        binaries.executable()
        browser {

        }
    }
}