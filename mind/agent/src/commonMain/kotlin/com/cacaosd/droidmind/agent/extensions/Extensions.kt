package com.cacaosd.droidmind.agent.extensions

import ai.koog.prompt.llm.LLModel
import com.agentclientprotocol.annotations.UnstableApi
import com.agentclientprotocol.model.ModelId
import com.agentclientprotocol.model.ModelInfo

@OptIn(UnstableApi::class)
fun LLModel.toAcpModelInfo(): ModelInfo = ModelInfo(
    modelId = ModelId(this.id),
    name = this.id,
)
