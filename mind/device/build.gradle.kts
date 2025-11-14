import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

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
            implementation(project(":mind:layout"))
            implementation(libs.platform.coroutines)
            implementation(libs.kotlinx.coroutines.core)
        }

        commonTest.dependencies {
        }

        val desktopMain by getting {
            dependencies {
                implementation(libs.android.tools.ddms)
                implementation(libs.android.tools.adblib)
                implementation(libs.android.tools.sdklib)
                implementation(libs.android.tools.common)
                implementation(libs.android.tools.sdk.common)
            }
        }

        val desktopTest by getting {
            dependencies {
                implementation(libs.kotlin.test.junit)
            }
        }
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.add("-Xopt-in=kotlin.time.ExperimentalTime")
    }
}
