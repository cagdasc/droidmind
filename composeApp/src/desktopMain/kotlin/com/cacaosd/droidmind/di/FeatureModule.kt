package com.cacaosd.droidmind.di

import com.cacaosd.droidmind.agent.di.AgentEventFlowQualifier
import com.cacaosd.droidmind.domain.AgentEvent
import com.cacaosd.droidmind.domain.session.ScenarioExecutor
import com.cacaosd.droidmind.feature.automation_runner.AutomationRunnerViewModel
import com.cacaosd.droidmind.feature.automation_runner.usecase.DevicePollUseCase
import com.cacaosd.droidmind.feature.automation_runner.usecase.InstalledAppsPollUseCase
import com.cacaosd.droidmind.feature.automation_runner.usecase.PollUseCase
import com.cacaosd.droidmind.mind.device.di.AndroidDeviceControllerQualifier
import kotlinx.coroutines.flow.MutableSharedFlow
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val featureModule = module {
    factory { PollUseCase() }
    single {
        DevicePollUseCase(
            pollUseCase = get(),
            deviceController = get(AndroidDeviceControllerQualifier),
            platformDispatchers = get()
        )
    }
    single {
        InstalledAppsPollUseCase(
            pollUseCase = get(),
            deviceController = get(AndroidDeviceControllerQualifier),
            platformDispatchers = get()
        )
    }

    viewModel {
        val scenarioExecutor: ScenarioExecutor = get()

        val agentEventFlow: MutableSharedFlow<AgentEvent> =
            get<MutableSharedFlow<AgentEvent>>(AgentEventFlowQualifier)

        AutomationRunnerViewModel(
            scenarioExecutor = scenarioExecutor,
            agentEventFlow = agentEventFlow,
            deviceController = get(AndroidDeviceControllerQualifier),
            devicePollUseCase = get(),
            installedAppsPollUseCase = get(),
            platformDispatchers = get()
        )
    }
}
