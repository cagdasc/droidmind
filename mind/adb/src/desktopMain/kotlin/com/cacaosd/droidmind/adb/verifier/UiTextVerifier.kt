package com.cacaosd.droidmind.adb.verifier

import com.cacaosd.droidmind.adb.device_controller.DeviceController
import com.cacaosd.droidmind.core.coroutine.DispatcherProvider
import kotlinx.coroutines.withContext

class UiTextVerifier(
    private val dispatcherProvider: DispatcherProvider,
    private val deviceController: DeviceController
) : Verifier {
    override suspend fun verify(
        serial: String,
        packageName: String,
        expectation: Expectation
    ): VerificationResult {
        val uiText = withContext(dispatcherProvider.IO) {
            deviceController.getUiDump(packageName, serial)
        }

        val passed = uiText.contains(expectation.value)
        val message = if (passed) {
            "Expectation met: UI contains '${expectation.value}'"
        } else {
            "Expectation not met: UI does not contain '${expectation.value}'"
        }
        return VerificationResult(passed, message)
    }
}
