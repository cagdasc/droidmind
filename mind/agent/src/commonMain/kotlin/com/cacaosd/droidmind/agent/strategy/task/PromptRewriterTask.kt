package com.cacaosd.droidmind.agent.strategy.task

import ai.koog.agents.ext.agent.subgraphWithTask
import com.cacaosd.droidmind.agent.strategy.PromptClassification

internal fun promptRewriterTask() = subgraphWithTask<PromptClassification, String>(
    name = "rewrite_prompt",
    tools = emptyList()
) { promptClassification ->
    """
    Phase: Prompt rewriting.
    Rewrite the original request into a machine-readable ordered execution plan. 
    Be careful about below points:
    * Launching app is not an INTERACTION step and visibility of app is not a VERIFICATION step.
    They have already done before plan is started.
    * If the user requests a device interaction such as search, do not handle the search steps separately such as
    Click search field, type the text and search it(by tapping search icon or enter). It is contextual command.
    Identify if there are commands like that and combine in one step until you identify another step.
     
    Output JSON only.
    Accept format below. If you find any special char, escape them if needed. 

    (StrategyExecutionPlanV2): { "request":"ORIGINAL_CONTENT", "steps": [{"id":"s1","type":"INTERACTION|VERIFICATION","content":"...","maxRetries":1}], "summary":"..." }

    The ordered steps may interleave interaction and verification steps. Include precisely what
    should be done or checked in each step's content field.
    
    Return only the JSON payload.

    Original request: ${promptClassification.request}
    """.trimIndent()
}
