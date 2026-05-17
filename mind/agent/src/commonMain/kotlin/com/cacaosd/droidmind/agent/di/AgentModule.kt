package com.cacaosd.droidmind.agent.di

import ai.koog.agents.core.tools.ToolRegistry
import com.cacaosd.droidmind.agent.client.DefaultAgentClientFactory
import com.cacaosd.droidmind.agent.session.DefaultScenarioExecutor
import com.cacaosd.droidmind.agent.strategy.reasoningStrategy
import com.cacaosd.droidmind.agent.tools.DeviceControllerTools
import com.cacaosd.droidmind.agent.tools.TestCaseVerifierTools
import com.cacaosd.droidmind.agent.tools.UiHierarchyTools
import com.cacaosd.droidmind.agent.tools.UiInteractionTools
import com.cacaosd.droidmind.core.config.di.SelfResolveQualifier
import com.cacaosd.droidmind.domain.AgentClientFactory
import com.cacaosd.droidmind.domain.session.ScenarioExecutor
import com.cacaosd.droidmind.mind.device.di.AndroidDeviceControllerQualifier
import com.cacaosd.droidmind.mind.verifier.Verifier
import org.koin.dsl.bind
import org.koin.dsl.module
import java.util.*

data object AgentMessageFlowQualifier : SelfResolveQualifier()

data object AgentEventFlowQualifier : SelfResolveQualifier()

val agentModule = module {
    single {
        DefaultAgentClientFactory(
            toolRegistry = get(),
            aiAgentStrategy = get(),
            eventMapper = get(),
            agentMessageFlow = get(AgentMessageFlowQualifier),
            agentEventFlow = get(AgentEventFlowQualifier),
            properties = get<Properties>(),
            clock = get()
        )
    } bind AgentClientFactory::class

    single {
        DefaultScenarioExecutor(
            deviceController = get(AndroidDeviceControllerQualifier)
        )
    } bind ScenarioExecutor::class
}

val agentToolsModule = module {
    single { DeviceControllerTools(deviceController = get(AndroidDeviceControllerQualifier)) }
    single { UiHierarchyTools(deviceController = get(AndroidDeviceControllerQualifier)) }
    single { UiInteractionTools(deviceController = get(AndroidDeviceControllerQualifier)) }
    single {
        TestCaseVerifierTools(
            verifier = get<Verifier>(),
            deviceController = get(AndroidDeviceControllerQualifier)
        )
    }

    single<ToolRegistry> {
        ToolRegistry {
            tools(toolsList = get<DeviceControllerTools>().asTools())
            tools(toolsList = get<UiHierarchyTools>().asTools())
            tools(toolsList = get<UiInteractionTools>().asTools())
//            tool(ExitTool)
            tools(toolsList = get<TestCaseVerifierTools>().asTools())
        }
    }
//    single { toolExecutionStrategy("Adb tool execution strategy") }
    single { reasoningStrategy() }
//    single { chatAgentStrategy() }
}
