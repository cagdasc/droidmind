package com.cacaosd.droidmind.adb.device_controller

import com.cacaosd.droidmind.core.AppConfigManager
import java.time.Clock

interface DeviceController {

    suspend fun getDevices(): List<DeviceInfo>

    suspend fun listInstalledPackages(serial: String?): List<String>

    suspend fun launchApp(packageName: String, serial: String?): String

    suspend fun getUiDump(packageName: String,serial: String?): String

    suspend fun inputText(text: String, serial: String?): String

    suspend fun tap(x: Int, y: Int, serial: String?): String

    suspend fun sendKeyEvent(key: String, serial: String?): String

    suspend fun deviceSize(serial: String?): String

    suspend fun screenshot(serial: String?): String

    suspend fun swipe(startX: Int, startY: Int, endX: Int, endY: Int, durationMs: Long = 300, serial: String?): String

    suspend fun enableAccessibilityService(serial: String?): Boolean

    suspend fun disableAccessibilityService(serial: String?): Boolean

    suspend fun sendData(serial: String?, values: Map<String, String>)
}

expect fun getAndroidDeviceController(appConfigManager: AppConfigManager, clock: Clock): DeviceController
