import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    jvm()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":core:config"))
                implementation(project(":domain:local"))
                implementation(project(":domain:result"))

                implementation(libs.platform.coroutines)
                implementation(libs.platform.coroutines.di)

                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.serialization.json)

                implementation(libs.room.runtime)
                implementation(libs.room.sqlite)
                implementation(libs.room.sqlite.bundled)

                implementation(libs.koin.core)
            }
        }

        jvmMain.dependencies {}

    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}

room {
    schemaDirectory("$projectDir/schemas")
    generateKotlin = true
}

dependencies {
    add("kspJvm", libs.room.compiler)
}