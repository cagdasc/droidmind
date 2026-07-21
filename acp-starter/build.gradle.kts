plugins {
    kotlin("jvm")
    application
}

group = "com.cacaosd.droidmind"
version = "0.0.1"

repositories {
}

application {
    mainClass = "com.cacaosd.droidmind.agent_starter.MainKt"
}

dependencies {
    implementation(project(":core:config"))
    implementation(project(":core:logging"))
    implementation(project(":mind:device"))
    implementation(project(":mind:layout"))
    implementation(project(":mind:verifier"))
    implementation(project(":mind:agent"))
    implementation(project(":mind:domain"))

    implementation(libs.platform.coroutines)
    implementation(libs.platform.coroutines.di)

    implementation(libs.koog.agents)

    implementation(libs.koin.core)
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}