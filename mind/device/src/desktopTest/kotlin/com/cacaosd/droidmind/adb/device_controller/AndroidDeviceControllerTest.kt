package com.cacaosd.droidmind.adb.device_controller

import com.cacaosd.droidmind.core.config.AppConfigManager
import com.cacaosd.droidmind.mind.device.controller.AndroidDeviceController
import com.cacaosd.droidmind.mind.device.controller.getAndroidDeviceController
import com.cacaosd.droidmind.mind.layout.model.OptimisedHierarchy
import com.cacaosd.droidmind.mind.layout.parser.LayoutParser
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class AndroidDeviceControllerTest {

    private val fixedClock = kotlin.time.Clock.System
    private val androidDeviceController = getAndroidDeviceController(
        AppConfigManager(
            appName = "mcpformobile",
            appVersion = "0.0.1",
            packageName = "com.cacaosd.droidmind",
            clock = fixedClock
        ),
        layoutParser = object : LayoutParser {
            override fun parse(uiDumpFile: File): OptimisedHierarchy {
                return OptimisedHierarchy(
                    rotation = com.cacaosd.droidmind.mind.layout.model.ScreenRotation.PORTRAIT,
                    root = com.cacaosd.droidmind.mind.layout.model.UiElement(
                        type = com.cacaosd.droidmind.mind.layout.model.ElementType.Button,
                        text = "Test Button",
                        contentDescription = "A button for testing",
                        bounds = com.cacaosd.droidmind.mind.layout.model.Rect(0, 0, 100, 50),
                        clickable = true,
                        focusable = true,
                        enabled = true,
                        children = emptyList()
                    )
                )
            }
        },
        clock = fixedClock
    ) as AndroidDeviceController

    @Test
    fun testListInstalledPackagesReturnsPackages(): Unit = runBlocking {
        val listInstalledPackages = androidDeviceController.getInstalledPackages("emulator-5554")
        listInstalledPackages.forEach {
            val appLabel = androidDeviceController.getAppLabel("emulator-5554", it)
            println(appLabel)
        }
    }

    @Test
    fun testUiDump(): Unit = runBlocking {
        androidDeviceController.getNativeUiDumpFile("com.nutmeg.app", "48261FDAS000D9")
    }

    @Test
    fun testEnableAccessibilityService(): Unit = runBlocking {
        val enableAccessibilityService = androidDeviceController.enableAccessibilityService("emulator-5554")
        println(enableAccessibilityService)
    }

    @Test
    fun testDisableAccessibilityService(): Unit = runBlocking {
        val disableAccessibilityService = androidDeviceController.disableAccessibilityService("emulator-5554")
        println(disableAccessibilityService)
    }

    @Test
    fun startInteractionFor20Seconds() = runBlocking {
        val enableAccessibilityService = androidDeviceController.enableAccessibilityService("emulator-5554")
        println("Accessibility Service Enabled: $enableAccessibilityService")

        androidDeviceController.sendData(
            "emulator-5554", mapOf(
                "INTERACTION_EVENT" to "start_recording",
                "APP_PACKAGE" to "com.google.android.youtube"
            )
        )

        delay(20.seconds)

        androidDeviceController.sendData(
            "emulator-5554", mapOf(
                "INTERACTION_EVENT" to "stop_recording",
                "APP_PACKAGE" to "com.google.android.youtube"
            )
        )

        val disableAccessibilityService = androidDeviceController.disableAccessibilityService("emulator-5554")
        println("Accessibility Service Disabled: $disableAccessibilityService")
    }
}
