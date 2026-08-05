package com.cacaosd.droidmind.agent.tools.di

import ai.koog.agents.core.tools.ToolRegistry
import com.cacaosd.droidmind.agent.tools.*
import com.cacaosd.droidmind.mind.device.di.AndroidDeviceManagerQualifier
import com.cacaosd.droidmind.mind.device.di.deviceModule
import com.cacaosd.droidmind.mind.verifier.Verifier
import com.cacaosd.droidmind.mind.verifier.di.verifierModule
import org.koin.dsl.module

internal val toolsModule = module {
    includes(deviceModule, verifierModule)

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
}