package com.cacaosd.droidmind.agent.di

import ai.koog.agents.core.tools.ToolRegistry
import com.cacaosd.droidmind.agent.acp.DroidMindAgentMain
import com.cacaosd.droidmind.agent.client.DefaultAgentClientFactory
import com.cacaosd.droidmind.agent.session.DefaultScenarioExecutor
import com.cacaosd.droidmind.agent.strategy.OneShotDeviceInteractionStrategy
import com.cacaosd.droidmind.agent.strategy.SteppedDeviceInteractionStrategy
import com.cacaosd.droidmind.agent.tools.*
import com.cacaosd.droidmind.core.config.di.SelfResolveQualifier
import com.cacaosd.droidmind.domain.AgentClientFactory
import com.cacaosd.droidmind.domain.session.ScenarioExecutor
import com.cacaosd.droidmind.mind.device.di.AndroidDeviceManagerQualifier
import com.cacaosd.droidmind.mind.verifier.Verifier
import org.koin.dsl.bind
import org.koin.dsl.module
import java.util.*

data object AgentEventFlowQualifier : SelfResolveQualifier()
data object OneshotInteractionVerificationQualifier : SelfResolveQualifier()
data object SteppedInteractionVerificationQualifier : SelfResolveQualifier()

val agentModule = module {
    single {
        DroidMindAgentMain(
            clock = get(),
            toolRegistry = get(),
            aiAgentStrategy = get(SteppedInteractionVerificationQualifier),
            properties = get<Properties>(),
            platformDispatchers = get()
        )
    }
    single {
        DefaultAgentClientFactory(
            toolRegistry = get(),
            aiAgentStrategy = get(OneshotInteractionVerificationQualifier),
            agentEventFlow = get(AgentEventFlowQualifier),
            properties = get<Properties>(),
            clock = get()
        )
    } bind AgentClientFactory::class

    single {
        DefaultScenarioExecutor(
            deviceController = get(AndroidDeviceManagerQualifier)
        )
    } bind ScenarioExecutor::class
}

val agentToolsModule = module {
    single { DeviceManagerTools(deviceController = get(AndroidDeviceManagerQualifier)) }
    single { UiHierarchyTools(deviceController = get(AndroidDeviceManagerQualifier)) }
    single { UiInteractionTools(deviceController = get(AndroidDeviceManagerQualifier)) }
    single { DeviceInfoTools(deviceController = get(AndroidDeviceManagerQualifier)) }
    single {
        UiVerifierTools(
            verifier = get<Verifier>(),
            deviceController = get(AndroidDeviceManagerQualifier)
        )
    }

    single<ToolRegistry> {
        ToolRegistry {
            tools(toolsList = get<DeviceManagerTools>().asTools())
            tools(toolsList = get<DeviceInfoTools>().asTools())
            tools(toolsList = get<UiHierarchyTools>().asTools())
            tools(toolsList = get<UiInteractionTools>().asTools())
            tools(toolsList = get<UiVerifierTools>().asTools())
        }
    }

    single(qualifier = OneshotInteractionVerificationQualifier) {
        OneShotDeviceInteractionStrategy(
            deviceManagerTools = get(),
            deviceInfoTools = get(),
            uiHierarchyTools = get(),
            uiInteractionTools = get()
        ).createStrategy()
    }

    single(qualifier = SteppedInteractionVerificationQualifier) {
        SteppedDeviceInteractionStrategy(
            deviceManagerTools = get(),
            deviceInfoTools = get(),
            uiHierarchyTools = get(),
            uiInteractionTools = get()
        ).createStrategy()
    }
}
