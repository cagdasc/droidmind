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
            implementation(libs.koog.agents)
            implementation(libs.koog.agents.acp)
            implementation(libs.koog.agents.client.google)
            implementation(libs.kotlinx.datetime)
            implementation(libs.koin.core)
            implementation(libs.platform.coroutines)
        }
    }
}
