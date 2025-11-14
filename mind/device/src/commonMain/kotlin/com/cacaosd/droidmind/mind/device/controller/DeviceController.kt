package com.cacaosd.droidmind.mind.device.controller

import com.cacaosd.droidmind.core.config.AppConfigManager
import com.cacaosd.droidmind.mind.device.info.DeviceInfo
import com.cacaosd.droidmind.mind.layout.model.OptimisedHierarchy
import com.cacaosd.droidmind.mind.layout.optimizer.LayoutOptimiser
import java.io.File
import java.time.Clock

interface DeviceController {

    suspend fun getDevices(): List<DeviceInfo>

    suspend fun listInstalledPackages(serial: String?): List<String>

    suspend fun launchApp(packageName: String, serial: String?): String

    suspend fun getUiDumpFile(packageName: String, serial: String?): File?

    suspend fun getOptimisedUiHierarchy(packageName: String, serial: String?): OptimisedHierarchy?

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

expect fun getAndroidDeviceController(
    appConfigManager: AppConfigManager,
    layoutOptimiser: LayoutOptimiser,
    clock: Clock
): DeviceController
