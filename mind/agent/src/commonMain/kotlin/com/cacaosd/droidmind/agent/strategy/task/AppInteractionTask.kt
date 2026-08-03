package com.cacaosd.droidmind.agent.strategy.task

import ai.koog.agents.core.tools.ToolBase
import ai.koog.agents.ext.agent.subgraphWithTask
import com.cacaosd.droidmind.agent.strategy.InteractionResult
import com.cacaosd.droidmind.agent.strategy.ProvisioningResult

internal fun appInteractionTask(tools: List<ToolBase<*, *>>) =
    subgraphWithTask<ProvisioningResult, InteractionResult>(
        name = "interact_with_app",
        tools = tools
    ) { provisioning ->
        """
            Phase: UI interaction.
            Execute the requested actions on the current screen. Read the UI hierarchy first to find
            the right elements/text, then perform the interaction.
            Describe precisely what you did so it can be checked afterwards.
            Use the step content as the Request field.

            Request: ${provisioning.request}
            """.trimIndent()
    }
