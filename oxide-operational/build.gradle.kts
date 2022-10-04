plugins {
    kotlin("jvm") version "1.7.20" apply false
    kotlin("js") version "1.7.20" apply false
}

allprojects {
    group = "me.cubuspl42"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}
