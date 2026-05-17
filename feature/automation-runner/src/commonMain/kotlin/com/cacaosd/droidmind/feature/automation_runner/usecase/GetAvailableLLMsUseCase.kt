package com.cacaosd.droidmind.feature.automation_runner.usecase

import com.cacaosd.droidmind.domain.AgentClientFactory
import com.cacaosd.droidmind.domain.ModelType
import com.cacaosd.droidmind.feature.automation_runner.LLMData
import com.cacaosd.platform.coroutines.dispatchers.PlatformDispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import com.cacaosd.droidmind.feature.automation_runner.ModelType as ModelTypeUi

class GetAvailableLLMsUseCase(
    private val agentClientFactory: AgentClientFactory,
    private val platformDispatchers: PlatformDispatchers
) {

    suspend operator fun invoke(): List<LLMData> = coroutineScope {
        val agentClients = withContext(platformDispatchers.io) {
            (agentClientFactory.createRemoteModel() + agentClientFactory.createLocalAgents())
        }

        withContext(platformDispatchers.default) {
            agentClients
                .map { agentClient ->
                    LLMData(
                        providerName = agentClient.modelProvider,
                        modelName = agentClient.modelName,
                        modelType = when (agentClient.modelType) {
                            ModelType.LOCAL -> ModelTypeUi.Local
                            ModelType.REMOTE -> ModelTypeUi.Remote
                        },
                        agentClient = agentClient
                    )
                }
        }
    }
}
