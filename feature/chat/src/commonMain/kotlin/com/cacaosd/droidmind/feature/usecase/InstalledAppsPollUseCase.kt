package com.cacaosd.droidmind.feature.usecase

import com.cacaosd.droidmind.mind.device.controller.DeviceController
import com.cacaosd.platform.coroutines.dispatchers.PlatformDispatchers
import kotlinx.coroutines.withContext

class InstalledAppsPollUseCase(
    private val pollUseCase: PollUseCase,
    private val deviceController: DeviceController,
    private val platformDispatchers: PlatformDispatchers
) {

    fun pollInstalledApps(
        deviceSerial: String,
        interval: Long = 20_000L
    ) = pollUseCase.poll(interval) {
        withContext(platformDispatchers.io) {
            deviceController.listInstalledPackages(deviceSerial)
        }
    }
}
