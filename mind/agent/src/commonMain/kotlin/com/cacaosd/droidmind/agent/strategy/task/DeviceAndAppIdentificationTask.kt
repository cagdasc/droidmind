package com.cacaosd.droidmind.agent.strategy.task

import ai.koog.agents.core.tools.ToolBase
import ai.koog.agents.ext.agent.subgraphWithTask
import com.cacaosd.droidmind.agent.strategy.ProvisioningResult

internal fun deviceAndAppIdentificationTask(tools: List<ToolBase<*, *>>) = subgraphWithTask<String, ProvisioningResult>(
    name = "identify_device_and_app",
    tools = tools
) { request ->
    """
            Phase: Device identification.
            Identify the target emulator/device and launch the app so it is
            in the foreground and ready for interaction.
            If this cannot be achieved, report `ready = false` with a clear `reason` instead of guessing.
            Always copy the original request verbatim into the `request` field.

            Request: $request
            """.trimIndent()
}
