package com.cacaosd.droidmind.domain.session

import com.cacaosd.droidmind.domain.AgentClient

interface ScenarioExecutor {
    suspend fun execute(agentClient: AgentClient, request: ScenarioExecutionRequest) {
        execute(
            agentClient = agentClient,
            deviceSerial = request.deviceSerial,
            packageName = request.packageName,
            prompt = request.scenario
        )
    }

    suspend fun execute(agentClient: AgentClient, deviceSerial: String?, packageName: String, prompt: String)

    suspend fun cancel(agentClient: AgentClient)
}