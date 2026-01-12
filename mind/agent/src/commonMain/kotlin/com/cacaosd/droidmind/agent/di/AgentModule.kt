package com.cacaosd.droidmind.agent.di

import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.reflect.asTools
import com.cacaosd.droidmind.agent.client.DefaultAgentClientFactory
import com.cacaosd.droidmind.agent.session.DefaultScenarioExecutor
import com.cacaosd.droidmind.agent.toolExecutionStrategy
import com.cacaosd.droidmind.agent.tools.DeviceControllerTools
import com.cacaosd.droidmind.agent.tools.TestCaseVerifierTools
import com.cacaosd.droidmind.core.config.di.SelfResolveQualifier
import com.cacaosd.droidmind.domain.AgentClientFactory
import com.cacaosd.droidmind.domain.session.ScenarioExecutor
import com.cacaosd.droidmind.mind.device.di.AndroidDeviceControllerQualifier
import com.cacaosd.droidmind.mind.verifier.Verifier
import org.koin.dsl.bind
import org.koin.dsl.module
import java.util.*

internal object GoogleAgentQualifier : SelfResolveQualifier()

internal object MetaAgentQualifier : SelfResolveQualifier()

internal object CustomAgentQualifier : SelfResolveQualifier()

object AgentMessageFlowQualifier : SelfResolveQualifier()

data object AgentEventFlowQualifier : SelfResolveQualifier()

val agentModule = module {
    single {
        DefaultAgentClientFactory(
            toolRegistry = get(),
            aiAgentStrategy = get(),
            eventMapper = get(),
            agentMessageFlow = get(AgentMessageFlowQualifier),
            agentEventFlow = get(AgentEventFlowQualifier),
            clock = get()
        )
    } bind AgentClientFactory::class

    single(GoogleAgentQualifier) {
        val agentClientFactory = get<AgentClientFactory>()
        val localProperties = get<Properties>()
        agentClientFactory.createGoogleAgent(localProperties.getProperty("GEMINI_API_KEY"))
    }
    single(MetaAgentQualifier) {
        val agentClientFactory = get<AgentClientFactory>()
        agentClientFactory.createMetaLLamaAgent()
    }
    single(CustomAgentQualifier) {
        val agentClientFactory = get<AgentClientFactory>()
        agentClientFactory.createCustomModel("qwen3:14b")
    }

    single {
        DefaultScenarioExecutor(
            agentClient = get(GoogleAgentQualifier),
            deviceController = get(AndroidDeviceControllerQualifier)
        )
    } bind ScenarioExecutor::class
}

val agentToolsModule = module {
    single { DeviceControllerTools(deviceController = get(AndroidDeviceControllerQualifier)) }
    single {
        TestCaseVerifierTools(
            verifier = get<Verifier>(),
            deviceController = get(AndroidDeviceControllerQualifier)
        )
    }

    single<ToolRegistry> {
        ToolRegistry {
            tools(toolsList = get<DeviceControllerTools>().asTools())
            tools(toolsList = get<TestCaseVerifierTools>().asTools())
        }
    }
    single { toolExecutionStrategy("Adb tool execution strategy") }
}
