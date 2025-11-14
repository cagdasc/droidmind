package com.cacaosd.droidmind.verifier.verifier

import com.cacaosd.droidmind.mind.device.controller.DeviceController
import com.cacaosd.droidmind.mind.layout.model.Element
import com.cacaosd.droidmind.mind.layout.model.flattenDfs
import com.cacaosd.droidmind.mind.verifier.Expectation
import com.cacaosd.droidmind.mind.verifier.VerificationResult
import com.cacaosd.droidmind.mind.verifier.Verifier
import com.cacaosd.platform.coroutines.dispatchers.PlatformDispatchers
import kotlinx.coroutines.withContext

class UiTextVerifier(
    private val platformDispatchers: PlatformDispatchers,
    private val deviceController: DeviceController
) : Verifier {
    override suspend fun verify(
        serial: String,
        packageName: String,
        expectation: Expectation
    ): VerificationResult {
        val uiHierarchy = withContext(platformDispatchers.io) {
            deviceController.getOptimisedUiHierarchy(packageName, serial)
        }?.let { optimisedHierarchy ->
            withContext(platformDispatchers.default) {
                optimisedHierarchy.flattenDfs { it.type is Element.TextBased }
            }
        } ?: return VerificationResult(
            false,
            "Failed to retrieve UI hierarchy for package '$packageName' on device '$serial'."
        )

        val result = withContext(platformDispatchers.default) {
            uiHierarchy.filter {
                it.text?.contains(expectation.value) ?: false
            }
        }
        val hasMatch = result.isNotEmpty()

        val message = if (hasMatch) {
            "UiText-Expectation met: UI contains '${expectation.value}'"
        } else {
            "UiText-Expectation not met: UI does not contain '${expectation.value}'"
        }
        return VerificationResult(passed = hasMatch, message = message)
    }
}
