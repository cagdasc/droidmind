package com.cacaosd.droidmind.agent.strategy.task

import ai.koog.agents.ext.agent.subgraphWithTask
import com.cacaosd.droidmind.agent.strategy.PromptClassification

fun classifyRequestTask() = subgraphWithTask<String, PromptClassification>(
    name = "classify_request",
    tools = emptyList()
) { request ->
    """
    Phase: Request classification.
    Decide whether the request below is about testing, inspecting, or controlling a mobile
    (Android) application or device: launching/installing an app, tapping/typing/scrolling on
    screen, reading UI state, verifying on-screen behavior, or managing an emulator/device.
    If it's unrelated (general chit-chat, unrelated coding help, unrelated questions, etc.),
    set inScope = false and give a brief, user-facing reason.
    set requiresVerification = true if the request explicitly or implicitly asks to verify,
    Do not attempt to fulfill the request yourself here - only classify it.
    Always copy the original request verbatim into the `request` field.

    Request: $request
    """.trimIndent()
}
