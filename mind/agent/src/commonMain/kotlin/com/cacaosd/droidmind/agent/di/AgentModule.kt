package com.cacaosd.droidmind.agent.di

import ai.koog.agents.core.tools.ToolRegistry
import com.cacaosd.droidmind.agent.acp.DroidMindAgentMain
import com.cacaosd.droidmind.agent.client.DefaultAgentClientFactory
import com.cacaosd.droidmind.agent.session.DefaultScenarioExecutor
import com.cacaosd.droidmind.agent.strategy.AndroidAgentClient
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

data object AgentEventFlowQualifier : SelfResolveQualifier()

val agentModule = module {
    single {
        DroidMindAgentMain(
            clock = get(),
            toolRegistry = get(),
            aiAgentStrategy = get(),
            properties = get<Properties>(),
            platformDispatchers = get()
        )
    }
    single {
        DefaultAgentClientFactory(
            toolRegistry = get(),
            aiAgentStrategy = get(),
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
            tools(toolsList = get<TestCaseVerifierTools>().asTools())
        }
    }

    single { AndroidAgentClient(get(), get(), get()).createStrategy() }
}
