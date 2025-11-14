package com.cacaosd.droidmind.mind.device.info

data class DeviceInfo(
    val name: String,
    val serial: String,
    val batteryLevel: Int,
    val osVersion: String,
    val dimensions: Dimensions
) {
    data class Dimensions(val width: Int, val height: Int)
}