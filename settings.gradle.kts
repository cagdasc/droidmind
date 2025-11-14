rootProject.name = "DroidMind"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":composeApp")
include(":core:config")
include(":core:logging")
include(":feature:chat")
include(":ui:theme")
include(":mind:agent")
include(":mind:domain")
include(":interaction-engine")
include(":mind:device")
include(":mind:layout")
include(":mind:verifier")

includeBuild("multiplatform-shared") {
    dependencySubstitution {
        substitute(module("com.cacaosd.platform:core"))
            .using(project(":platform:core"))

        substitute(module("com.cacaosd.platform:core-di"))
            .using(project(":platform:core-di"))

        substitute(module("com.cacaosd.platform:coroutines"))
            .using(project(":platform:coroutines"))

        substitute(module("com.cacaosd.platform:coroutines-di"))
            .using(project(":platform:coroutines-di"))
    }
}
