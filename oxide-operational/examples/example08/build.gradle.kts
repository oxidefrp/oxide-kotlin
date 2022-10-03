plugins {
    kotlin("js")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":examples:common"))
}

kotlin {
    js(LEGACY) {
        binaries.executable()
        browser {

        }
    }
}
