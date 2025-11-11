package com.cacaosd.droidmind.agent.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import com.cacaosd.droidmind.adb.verifier.Expectation
import com.cacaosd.droidmind.adb.verifier.Verifier
import com.cacaosd.droidmind.agent.tools.description.TestCaseVerifierToolsConstant

@LLMDescription(TestCaseVerifierToolsConstant.TOOLSET_DESCRIPTION)
class TestCaseVerifierTools(private val verifier: Verifier) : ToolSet {

    @Tool(TestCaseVerifierToolsConstant.VERIFY_UI_TEXT_TOOL_NAME)
    @LLMDescription(TestCaseVerifierToolsConstant.VERIFY_UI_TEXT_TOOL_DESC)
    suspend fun verifyUiText(serial: String, packageName: String, expectedText: String): String {
        val expectation = Expectation.Text(value = expectedText)
        val result = verifier.verify(serial, packageName, expectation)
        return if (result.passed) {
            "Verification Passed: ${result.message}"
        } else {
            "Verification Failed: ${result.message}"
        }
    }
}
