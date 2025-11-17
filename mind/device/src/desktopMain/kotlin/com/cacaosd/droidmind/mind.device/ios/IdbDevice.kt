package com.cacaosd.droidmind.mind.device.ios

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IdbDevice(
    val name: String,
    val udid: String,
    val state: DeviceState?,
    val type: String,
    @SerialName("os_version")
    val osVersion: String,
    val architecture: String,
)


@Serializable
enum class DeviceState {
    @SerialName("Booted")
    Booted,

    @SerialName("Shutdown")
    Shutdown
}
