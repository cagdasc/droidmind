package com.cacaosd.droidmind.feature.usecase

import com.cacaosd.droidmind.mind.device.controller.DeviceController
import com.cacaosd.platform.coroutines.dispatchers.PlatformDispatchers
import kotlinx.coroutines.withContext

class DevicePollUseCase(
    private val pollUseCase: PollUseCase,
    private val deviceController: DeviceController,
    private val platformDispatchers: PlatformDispatchers
) {

    fun pollDeviceState(interval: Long = 5000L) = pollUseCase.poll(interval) {
        withContext(platformDispatchers.io) {
            deviceController.getDevices()
        }
    }
}
