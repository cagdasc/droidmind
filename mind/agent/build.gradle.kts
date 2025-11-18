plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    jvm("desktop")

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:config"))
            implementation(project(":core:logging"))
            implementation(project(":mind:device"))
            implementation(project(":mind:domain"))
            implementation(project(":mind:layout"))
            implementation(project(":mind:verifier"))
            implementation(libs.ai.koog)
            implementation(libs.kotlinx.datetime)
            implementation(libs.koin.core)
        }
    }
}
