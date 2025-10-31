plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    jvm("desktop")

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core"))
            implementation(project(":mind:adb"))
            implementation(project(":mind:domain"))
            implementation(libs.ai.koog)
            implementation(libs.kotlinx.datetime)
        }
    }
}
