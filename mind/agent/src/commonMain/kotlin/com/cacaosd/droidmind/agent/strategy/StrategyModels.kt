package com.cacaosd.droidmind.agent.strategy

import ai.koog.agents.core.tools.annotations.LLMDescription
import kotlinx.serialization.Serializable

/**
 * Structured result of Layer 0 (request classification / scope gate).
 */
@Serializable
@LLMDescription("Classification of whether a user request is in scope for a mobile app testing/device-control agent")
data class PromptClassification(
    @property:LLMDescription(
        "True only if the request is about testing, inspecting, or controlling a mobile app, " +
                "emulator, or device - e.g. launching/installing an app, tapping/typing/scrolling on screen, " +
                "reading UI state, verifying on-screen behavior, or managing an emulator/device"
    )
    val inScope: Boolean,
    @property:LLMDescription("The original request, copied verbatim, to hand off to later phases when in scope")
    val request: String,
    @property:LLMDescription(
        "True only if the request explicitly or implicitly asks to verify, check, confirm, or test the " +
                "outcome (e.g. contains wording like 'verify', 'check that', 'make sure', 'confirm', 'test that'). " +
                "False for a plain action request where no confirmation of the result was asked for."
    )
    val requiresVerification: Boolean = false,
    @property:LLMDescription("Short, user-facing explanation when inScope = false. Empty when inScope = true")
    val reason: String = ""
)

/**
 * Structured result of Layer 1 (device identification).
 * Carrying `request` forward means later layers don't depend on the LLM "remembering"
 * the original ask across subgraph boundaries.
 */
@Serializable
@LLMDescription("Result of locating the target device/emulator and preparing the app under test")
data class ProvisioningResult(
    @property:LLMDescription("True only if the device is ready and the app is in the foreground")
    val ready: Boolean,
    @property:LLMDescription("The original test request, copied verbatim so later phases keep context")
    val request: String,
    @property:LLMDescription("Reason provisioning failed. Empty when ready = true")
    val reason: String = ""
)

/**
 * Structured result of Layer 2 (UI interaction).
 */
@Serializable
@LLMDescription("Summary of the UI actions that were attempted on the current screen")
data class InteractionResult(
    @property:LLMDescription("The original test request, copied verbatim")
    val request: String,
    @property:LLMDescription("Precisely what was done on screen (taps, text entry, scrolls), detailed enough to verify")
    val summary: String
)
