package com.cacaosd.droidmind.domain.tools

object DeviceManagerToolsConstant {
    const val TOOLSET_DESCRIPTION =
        "A set of tools that helps to find connected devices, list available applications and launch an application."

    const val LIST_CONNECTED_DEVICES_TOOL = "list_connected_devices"
    const val LIST_CONNECTED_DEVICES_TOOL_DESC =
        "Retrieves the serial numbers of all devices and emulators currently connected."

    const val LIST_INSTALLED_PACKAGES_TOOL = "list_installed_packages"
    const val LIST_INSTALLED_PACKAGES_TOOL_DESC =
        "Retrieves all package names of applications installed on the specified device (defaults to the first connected device if omitted)."

    const val LAUNCH_APP_TOOL = "launch_app_by_package"
    const val LAUNCH_APP_TOOL_DESC =
        "Launches an Android app by its package name on the specified device."
}
