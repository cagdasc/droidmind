package com.cacaosd.droidmind.agent.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import com.cacaosd.droidmind.adb.verifier.Expectation
import com.cacaosd.droidmind.adb.verifier.Verifier

@LLMDescription("Tools for verifying test case results on Android devices.")
class TestCaseVerifierTools(private val verifier: Verifier) : ToolSet {

    @Tool("verify_ui_text")
    @LLMDescription("Verifies if the expected text is present in the UI of the specified Android application.")
    suspend fun verifyUiText(serial: String, packageName: String, expectedText: String): String {
        val expectation = Expectation(type = "ui_text", value = expectedText)
        val result = verifier.verify(serial, packageName, expectation)
        return if (result.passed) {
            "Verification Passed: ${result.message}"
        } else {
            "Verification Failed: ${result.message}"
        }
    }
}
