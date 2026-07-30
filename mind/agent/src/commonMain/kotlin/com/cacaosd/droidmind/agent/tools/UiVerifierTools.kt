package com.cacaosd.droidmind.agent.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import com.cacaosd.droidmind.domain.tools.UiVerifierToolsConstant
import com.cacaosd.droidmind.mind.device.controller.DeviceController
import com.cacaosd.droidmind.mind.verifier.Expectation
import com.cacaosd.droidmind.mind.verifier.Verifier

@LLMDescription(UiVerifierToolsConstant.TOOLSET_DESCRIPTION)
class UiVerifierTools(private val verifier: Verifier, private val deviceController: DeviceController) : ToolSet {

    @Tool(UiVerifierToolsConstant.VERIFY_UI_TEXT_TOOL_NAME)
    @LLMDescription(UiVerifierToolsConstant.VERIFY_UI_TEXT_TOOL_DESC)
    suspend fun verifyUiText(serial: String, packageName: String, expectedText: String): String {
        val optimisedUiHierarchy = deviceController.getOptimisedUiHierarchy(packageName = packageName, serial = serial)
            ?: return "Failed to retrieve UI hierarchy for package '$packageName' on device '$serial'."
        val expectation = Expectation.Text(value = expectedText)
        val result = verifier.verify(optimisedHierarchy = optimisedUiHierarchy, expectation = expectation)
        return if (result.passed) {
            "Verification Passed: ${result.message}"
        } else {
            "Verification Failed: ${result.message}"
        }
    }
}
