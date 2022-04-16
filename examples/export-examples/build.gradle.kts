import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    application
}

dependencies {
    implementation("org.eclipse.jgit:org.eclipse.jgit:6.1.0.202203080745-r")
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}
