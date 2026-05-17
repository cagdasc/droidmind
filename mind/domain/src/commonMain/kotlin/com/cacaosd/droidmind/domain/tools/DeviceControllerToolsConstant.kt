package com.cacaosd.droidmind.domain.tools

object DeviceControllerToolsConstant {
    const val TOOLSET_DESCRIPTION =
        "A set of tools to interact with Android devices. These tools allow you to retrieve connected devices, list installed packages, launch apps, get device screen size, and capture screenshots."

    const val LIST_CONNECTED_DEVICES_TOOL = "list_connected_devices"
    const val LIST_CONNECTED_DEVICES_TOOL_DESC =
        "Retrieves the serial numbers of all devices and emulators currently connected."

    const val LIST_INSTALLED_PACKAGES_TOOL = "list_installed_packages"
    const val LIST_INSTALLED_PACKAGES_TOOL_DESC =
        "Retrieves all package names of applications installed on the specified device (defaults to the first connected device if omitted)."

    const val LAUNCH_APP_TOOL = "launch_app_by_package"
    const val LAUNCH_APP_TOOL_DESC =
        "Launches an Android app by its package name on the specified device."

    const val DEVICE_SIZE_TOOL = "device_size"
    const val DEVICE_SIZE_TOOL_DESC =
        "Retrieves the screen size (width x height) of the specified Android device."

    const val SCREENSHOT_TOOL = "device_screenshot"
    const val SCREENSHOT_TOOL_DESC =
        "It captures screenshot of current screen on Android device and save it to local development machine."
}
