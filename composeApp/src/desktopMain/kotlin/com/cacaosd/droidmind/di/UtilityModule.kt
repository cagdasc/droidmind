package com.cacaosd.droidmind.di

import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.reflect.asTools
import com.cacaosd.droidmind.agent.event.EventMapper
import com.cacaosd.droidmind.agent.toolExecutionStrategy
import com.cacaosd.droidmind.agent.tools.DeviceControllerTools
import com.cacaosd.droidmind.agent.tools.TestCaseVerifierTools
import com.cacaosd.droidmind.core.config.AppConfigManager
import com.cacaosd.droidmind.domain.McpMessage
import com.cacaosd.droidmind.localProperties
import com.cacaosd.droidmind.mind.device.controller.DeviceController
import com.cacaosd.droidmind.mind.device.controller.getAndroidDeviceController
import com.cacaosd.droidmind.mind.device.controller.getIosDeviceController
import com.cacaosd.droidmind.mind.layout.optimizer.LayoutParser
import com.cacaosd.droidmind.mind.layout.optimizer.androidLayoutParser
import com.cacaosd.droidmind.mind.layout.optimizer.iOSLayoutParser
import com.cacaosd.droidmind.mind.verifier.Verifier
import com.cacaosd.droidmind.verifier.verifier.UiTextVerifier
import kotlinx.coroutines.flow.MutableSharedFlow
import org.koin.dsl.bind
import org.koin.dsl.module
import java.time.Clock
import java.util.*

val utilityModule = module {
    single { EventMapper() }
    single<MutableSharedFlow<McpMessage>>(qualifier = AgentMessageFlowQualifier) { MutableSharedFlow() }
    single<Properties> { localProperties }
    single<Clock> { Clock.systemUTC() }
}

val toolsModule = module {
    single {
        AppConfigManager(
            appName = "droidmind",
            appVersion = "0.0.1",
            packageName = "com.cacaosd.droidmind",
            clock = get()
        )
    }

    single(AndroidLayoutParserQualifier) { androidLayoutParser() } bind LayoutParser::class
    single(IosLayoutParserQualifier) { iOSLayoutParser() } bind LayoutParser::class

    single(AndroidDeviceControllerQualifier) {
        getAndroidDeviceController(
            appConfigManager = get(),
            layoutParser = get(),
            clock = get()
        )
    } bind DeviceController::class

    single {
        Json {
            prettyPrint = true          // formatted output
            isLenient = false            // allow non-strict JSON
            ignoreUnknownKeys = true    // ignore fields not in your class
            encodeDefaults = false       // include default values in output
            explicitNulls = false
            coerceInputValues = true
        }
    }


    single(IosDeviceControllerQualifier) {
        getIosDeviceController(json = get(), clock = get(), appConfigManager = get(), layoutParser = get())
    } bind DeviceController::class

    single<Verifier> {
        UiTextVerifier(
            platformDispatchers = get(),
            deviceController = get<DeviceController>(qualifier = AndroidDeviceControllerQualifier)
        )
    }

    single { DeviceControllerTools(get(AndroidDeviceControllerQualifier)) }
    single { TestCaseVerifierTools(verifier = get<Verifier>()) }

    single<ToolRegistry> {
        ToolRegistry {
            tools(toolsList = get<DeviceControllerTools>().asTools())
            tools(toolsList = get<TestCaseVerifierTools>().asTools())
        }
    }
    single { toolExecutionStrategy("Adb tool execution strategy") }
}

