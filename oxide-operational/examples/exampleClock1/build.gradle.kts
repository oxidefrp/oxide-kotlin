plugins {
    kotlin("js")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":examples:common"))
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.2")
}

kotlin {
    js(LEGACY) {
        binaries.executable()
        browser {

        }
    }
}
