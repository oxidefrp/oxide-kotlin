plugins {
    kotlin("js")
}

dependencies {
    implementation(project(":core"))
}

kotlin {
    js(LEGACY) {
        binaries.executable()
        browser {

        }
    }
}
