package com.cacaosd.droidmind.mind.device.controller

import kotlinx.serialization.Serializable

@Serializable
data class ScreenshotResult(
    val type: String = "image_url",
    val imageUrl: ImagePayload
)

@Serializable
data class ImagePayload(
    val url: String // Format: "data:image/png;base64,iVBORw0KG..."
)
