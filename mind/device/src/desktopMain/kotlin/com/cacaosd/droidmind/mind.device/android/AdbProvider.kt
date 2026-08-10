package com.cacaosd.droidmind.mind.device.android

import com.android.ddmlib.AndroidDebugBridge
import com.cacaosd.droidmind.core.logging.Logger
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.milliseconds

fun getAdb(androidHome: String): AndroidDebugBridge = runBlocking {
    AndroidDebugBridge.init(false)

    Logger.debug("ANDROID_HOME: $androidHome")

    val sdkHome = System.getenv("ANDROID_HOME") ?: androidHome

    AndroidDebugBridge.getBridge() ?: createAdb(androidHome = sdkHome)
}

private suspend fun createAdb(androidHome: String): AndroidDebugBridge = AndroidDebugBridge.createBridge(
    "$androidHome/platform-tools/adb",
    false,
    20,
    TimeUnit.SECONDS
).also { adb ->
    // Wait for initial device list
    repeat(10) {
        if (adb.hasInitialDeviceList()) return@also
        delay(500.milliseconds)
    }
}