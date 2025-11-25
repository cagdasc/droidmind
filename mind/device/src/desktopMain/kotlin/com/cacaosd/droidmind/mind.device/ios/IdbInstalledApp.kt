package com.cacaosd.droidmind.mind.device.ios

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IdbInstalledApp(
    @SerialName("bundle_id")
    val bundleId: String,
    val name: String,
)
