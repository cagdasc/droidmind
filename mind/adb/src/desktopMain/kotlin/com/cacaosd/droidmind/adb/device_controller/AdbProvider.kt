package com.cacaosd.droidmind.adb.device_controller

import com.android.ddmlib.AndroidDebugBridge
import com.cacaosd.droidmind.core.logging.Logger
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

fun getAdb(): AndroidDebugBridge = runBlocking {
    AndroidDebugBridge.init(false)

    val androidHome = System.getenv("ANDROID_HOME")
    if (androidHome.isNullOrEmpty()) error("ANDROID_HOME is not set")
    Logger.debug("ANDROID_HOME: $androidHome")

    AndroidDebugBridge.getBridge() ?: createAdb(androidHome)
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
        delay(500)
    }
}