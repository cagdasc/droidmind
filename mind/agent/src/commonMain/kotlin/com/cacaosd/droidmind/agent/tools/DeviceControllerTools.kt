package com.cacaosd.droidmind.agent.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import com.cacaosd.droidmind.adb.device_controller.DeviceController

@LLMDescription(
    "Tools for control Android device via Android Device Bridge(adb) which is a " +
            "commandline executable to run commands for android devices"
)
class DeviceControllerTools(private val deviceController: DeviceController) : ToolSet {

    @Tool("list_connected_devices")
    @LLMDescription(
        "Retrieves the serial numbers of all Android devices and emulators currently connected " +
                "to the local development machine via ADB"
    )
    suspend fun listConnectedDevices(): List<String> {
        return deviceController.getDevices().map { "Device name: ${it.name}, serial: ${it.serial}" }
    }

    @Tool("list_installed_packages")
    @LLMDescription("Retrieves all package names of applications installed on the specified device (defaults to the first connected device if omitted).")
    suspend fun listInstalledPackages(serial: String?): List<String> =
        deviceController.listInstalledPackages(serial = serial)

    @Tool("launch_app_by_package")
    @LLMDescription("Launches an Android app by its package name on the specified device.")
    suspend fun launchApp(packageName: String, serial: String?): String = deviceController.launchApp(
        packageName = packageName,
        serial = serial
    )

    @Tool("ui_dump")
    @LLMDescription("Retrieves the current UI hierarchy (in XML) from the Android device with coordinates by passing package name.")
    suspend fun uiDump(packageName: String, serial: String?): String =
        deviceController.getOptimisedUiHierarchy(packageName = packageName, serial = serial)?.toString().orEmpty()

    @Tool("input_text")
    @LLMDescription("Types and sends text input to the Android device using the ADB shell input command.")
    suspend fun inputText(text: String, serial: String?): String = deviceController.inputText(
        text = text,
        serial = serial
    )

    @Tool("tap")
    @LLMDescription("Simulates a tap at the specified (x, y) screen coordinates on the Android device.")
    suspend fun tap(x: Int, y: Int, serial: String?): String = deviceController.tap(x = x, y = y, serial = serial)

    @Tool("send_key_event")
    @LLMDescription(
        "Sends a key event to the Android device. Supported keys include: home, back, menu, search, enter, done, next, del, space, tab, up, down, left, right."
    )
    suspend fun sendKeyEvent(key: String, serial: String?): String =
        deviceController.sendKeyEvent(key = key, serial = serial)

    @Tool("device_size")
    @LLMDescription("Get device size on the specified device.")
    suspend fun deviceSize(serial: String?): String = deviceController.deviceSize(serial = serial)

    @Tool("device_screenshot")
    @LLMDescription("It captures screenshot of current screen on Android device and save it to local development machine.")
    suspend fun screenshot(serial: String?): String = deviceController.screenshot(serial = serial)

    @Tool("swipe")
    @LLMDescription(
        " Simulates a continuous swipe (drag) gesture from the start coordinates to the end coordinates over the specified duration " +
                "if startX and startY difference is lesser than endX and endY which means you are vertıcally scrolling otherwıse horizontal scrolling. " +
                "You can use device_size to to make sure you are in bounds. Also you can use get_ui_dump to check are you in given scroll condition."
    )
    suspend fun swipe(
        startX: Int,
        startY: Int,
        endX: Int,
        endY: Int,
        durationMs: Long,
        serial: String?
    ): String = deviceController.swipe(
        startX = startX,
        startY = startY,
        endX = endX,
        endY = endY,
        durationMs = durationMs,
        serial = serial
    )
}
