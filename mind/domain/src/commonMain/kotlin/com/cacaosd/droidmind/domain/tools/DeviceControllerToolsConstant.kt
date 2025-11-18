package com.cacaosd.droidmind.domain.tools

object DeviceControllerToolsConstant {
    const val TOOLSET_DESCRIPTION =
        "Tools for control Android device via Android Device Bridge(adb) which is a commandline executable to run commands for android devices"

    const val LIST_CONNECTED_DEVICES_TOOL = "list_connected_devices"
    const val LIST_CONNECTED_DEVICES_TOOL_DESC =
        "Retrieves the serial numbers of all Android devices and emulators currently connected."

    const val LIST_INSTALLED_PACKAGES_TOOL = "list_installed_packages"
    const val LIST_INSTALLED_PACKAGES_TOOL_DESC =
        "Retrieves all package names of applications installed on the specified device (defaults to the first connected device if omitted)."

    const val LAUNCH_APP_TOOL = "launch_app_by_package"
    const val LAUNCH_APP_TOOL_DESC =
        "Launches an Android app by its package name on the specified device."

    const val UI_DUMP_TOOL = "ui_dump"
    const val UI_DUMP_TOOL_DESC =
        "Retrieves the current UI hierarchy (in XML) from the Android device with coordinates by passing package name."

    const val INPUT_TEXT_TOOL = "input_text"
    const val INPUT_TEXT_TOOL_DESC =
        "Types and sends text input to the Android device using the ADB shell input command."

    const val TAP_TOOL = "tap"
    const val TAP_TOOL_DESC =
        "Taps at the specified (x, y) screen coordinates on the Android device."

    const val SEND_KEY_EVENT_TOOL = "send_key_event"
    const val SEND_KEY_EVENT_TOOL_DESC =
        "Sends a key event to the Android device. Supported keys include: home, back, menu, search, enter, done, next, del, space, tab, up, down, left, right."

    const val DEVICE_SIZE_TOOL = "device_size"
    const val DEVICE_SIZE_TOOL_DESC =
        "Retrieves the screen size (width x height) of the specified Android device."

    const val SWIPE_TOOL = "swipe"
    const val SWIPE_TOOL_DESC =
        "Performs a swipe gesture from the starting coordinates (startX, startY) to the ending coordinates (endX, endY) over a specified duration in milliseconds on the Android device. If the difference between startX and endX is greater than that of startY and endY, it indicates a horizontal swipe; otherwise, it's a vertical swipe."

    const val SCREENSHOT_TOOL = "device_screenshot"
    const val SCREENSHOT_TOOL_DESC =
        "It captures screenshot of current screen on Android device and save it to local development machine."
}
