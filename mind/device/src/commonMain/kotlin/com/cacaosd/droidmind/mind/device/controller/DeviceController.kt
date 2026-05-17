package com.cacaosd.droidmind.mind.device.controller

import com.cacaosd.droidmind.core.config.AppConfigManager
import com.cacaosd.droidmind.mind.device.info.DeviceInfo
import com.cacaosd.droidmind.mind.layout.model.OptimisedHierarchy
import com.cacaosd.droidmind.mind.layout.parser.LayoutParser
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.time.Clock

interface DeviceController {

    suspend fun getDevices(): List<DeviceInfo>

    suspend fun listInstalledPackages(serial: String?): List<String>

    suspend fun launchApp(packageName: String, serial: String?): String

    suspend fun getUiDumpFile(packageName: String, serial: String?): File?

    suspend fun getNativeUiDumpFile(packageName: String, serial: String?): File?

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
    layoutParser: LayoutParser,
    clock: Clock
): DeviceController

expect fun getIosDeviceController(
    json: Json,
    clock: Clock,
    appConfigManager: AppConfigManager,
    layoutParser: LayoutParser
): DeviceController
