plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    jvm("desktop")

    sourceSets {
        commonMain.dependencies {
            implementation(libs.logging.kotlin)
            implementation(libs.logging.logback.classic)
        }
    }
}
