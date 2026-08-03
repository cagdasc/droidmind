@file:OptIn(InternalAgentsApi::class)

package com.cacaosd.droidmind.agent.strategy.task

import ai.koog.agents.core.agent.entity.ToolSelectionStrategy
import ai.koog.agents.core.annotation.InternalAgentsApi
import ai.koog.agents.ext.agent.subgraphWithVerification
import ai.koog.serialization.typeToken
import com.cacaosd.droidmind.agent.strategy.InteractionResult

internal fun interactionSummarizeTask() = subgraphWithVerification<InteractionResult>(
    name = "summarize_result",
    inputType = typeToken<InteractionResult>(),
    toolSelectionStrategy = ToolSelectionStrategy.NONE
) { interaction ->
    """
    Phase: Summarize result.
    If you reach that stage everything should work fine. Summarize the user request and explain what you did.

    Request: ${interaction.request}
    """.trimIndent()
}