package com.cacaosd.droidmind.mind.device.controller

import com.cacaosd.droidmind.core.config.AppConfigManager
import com.cacaosd.droidmind.mind.device.info.DeviceInfo
import com.cacaosd.droidmind.mind.device.ios.IosDeviceBridge
import com.cacaosd.droidmind.mind.layout.model.OptimisedHierarchy
import com.cacaosd.droidmind.mind.layout.optimizer.LayoutParser
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import org.openqa.selenium.OutputType
import org.openqa.selenium.WebDriverException
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.time.Clock
import java.util.concurrent.TimeUnit

fun getIosDeviceController(
    json: Json,
    clock: Clock,
    appConfigManager: AppConfigManager,
    layoutParser: LayoutParser
): DeviceController =
    IosDeviceController(
        iOSDeviceBridge = IosDeviceBridge(json),
        clock = clock,
        appConfigManager = appConfigManager,
        layoutParser = layoutParser
    )

class IosDeviceController(
    private val iOSDeviceBridge: IosDeviceBridge,
    private val clock: Clock,
    private val appConfigManager: AppConfigManager,
    private val layoutParser: LayoutParser
) : DeviceController {
    override suspend fun getDevices(): List<DeviceInfo> {
        return iOSDeviceBridge.getDevices().map {
            DeviceInfo(
                name = it.name,
                serial = it.udid,
                osVersion = it.osVersion,
                batteryLevel = -1,
                dimensions = DeviceInfo.Dimensions(width = -1, height = -1)
            )
        }
    }

    override suspend fun listInstalledPackages(serial: String?): List<String> {
        val serial = serial ?: error("Serial cannot be null for iOS devices")
        return iOSDeviceBridge.getInstalledApps(serial).map { it.bundleId }
    }

    override suspend fun launchApp(packageName: String, serial: String?): String {
        val serial = serial ?: error("Serial cannot be null for iOS devices")
        val isLaunched = iOSDeviceBridge.launchApp(serial, packageName)
        return if (isLaunched) {
            "Launched $packageName"
        } else {
            "Failed to launch $packageName"
        }
    }

    override suspend fun getUiDumpFile(packageName: String, serial: String?): File? {
        val serial = serial ?: error("Serial cannot be null for iOS devices")
        val driver = iOSDeviceBridge.getDriver(serial)
        val pageSource = driver.pageSource

        val timestamp = TimeUnit.MILLISECONDS.toSeconds(clock.millis())
        val xmlName = "uidump_${packageName}_$timestamp.xml"

        delay(250) // Wait for the dump to be created

        return appConfigManager.getUiDumpFile(filename = xmlName).toFile().apply {
            writeText(pageSource.orEmpty())
        }
    }

    override suspend fun getOptimisedUiHierarchy(
        packageName: String,
        serial: String?
    ): OptimisedHierarchy? {
        val serial = serial ?: error("Serial cannot be null for iOS devices")
        val uiDumpFile = getUiDumpFile(packageName, serial) ?: return null
        return layoutParser.parse(uiDumpFile)
    }

    override suspend fun inputText(text: String, serial: String?): String {
        serial ?: error("Serial cannot be null for iOS devices")
        iOSDeviceBridge.sendInput(text)
        return "Input sent: $text"
    }

    override suspend fun tap(x: Int, y: Int, serial: String?): String {
        serial ?: error("Serial cannot be null for iOS devices")
        iOSDeviceBridge.tap(x, y)
        return "Tapped at ($x, $y)"
    }

    override suspend fun sendKeyEvent(key: String, serial: String?): String {
        serial ?: error("Serial cannot be null for iOS devices")
        val keyCode = keyEventMap[key.lowercase()] ?: error("Unsupported key event: $key")
        iOSDeviceBridge.sendKeyEvent(keyCode)
        return "Sent key event: $key"
    }

    override suspend fun deviceSize(serial: String?): String {
        val serial = serial ?: error("Serial cannot be null for iOS devices")
        val driver = iOSDeviceBridge.getDriver(serial)
        val size = driver.manage().window().size
        return "${size.width}x${size.height}"
    }

    override suspend fun screenshot(serial: String?): String {
        val serial = serial ?: error("Serial cannot be null for iOS devices")

        val timestamp = TimeUnit.MILLISECONDS.toSeconds(clock.millis())

        val driver = iOSDeviceBridge.getDriver(serial)
        driver.getScreenshotAs(
            object : OutputType<File> {
                override fun convertFromBase64Png(base64Png: String): File {
                    return save(OutputType.BYTES.convertFromBase64Png(base64Png))
                }

                override fun convertFromPngBytes(data: ByteArray): File {
                    return save(data)
                }

                private fun save(data: ByteArray): File {
                    try {
                        return appConfigManager.getScreenshotsFile("${timestamp}.png").apply {
                            Files.write(this, data)
                        }.toFile()
                    } catch (e: IOException) {
                        throw WebDriverException(e)
                    }
                }
            }
        )

        return "Screenshot file name is ${timestamp}.png"
    }

    override suspend fun swipe(
        startX: Int,
        startY: Int,
        endX: Int,
        endY: Int,
        durationMs: Long,
        serial: String?
    ): String {
        serial ?: error("Serial cannot be null for iOS devices")
        iOSDeviceBridge.swipe(startX, startY, endX, endY)
        return "Swiped from ($startX, $startY) to ($endX, $endY)"
    }

    override suspend fun enableAccessibilityService(serial: String?): Boolean {
        return true
    }

    override suspend fun disableAccessibilityService(serial: String?): Boolean {
        return true
    }

    override suspend fun sendData(
        serial: String?,
        values: Map<String, String>
    ) {
        // No-op for iOS
    }

    val keyEventMap = mapOf(
        "enter" to 0x28,
        "done" to 0x28,
        "next" to 0x2B,
        "tab" to 0x2B,
        "del" to 0x2A,
        "space" to 0x2C,
        "up" to 0x52,
        "down" to 0x51,
        "left" to 0x50,
        "right" to 0x4F
    )
}
