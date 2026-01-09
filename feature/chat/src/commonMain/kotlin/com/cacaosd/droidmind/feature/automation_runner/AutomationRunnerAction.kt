package com.cacaosd.droidmind.feature.automation_runner

import com.cacaosd.droidmind.feature.ChipItem
import com.cacaosd.droidmind.feature.DeviceData
import com.cacaosd.droidmind.feature.InstalledApp

sealed interface AutomationRunnerAction {
    data class DeviceSelected(val deviceData: DeviceData) : AutomationRunnerAction
    data class AppSelected(val installedApp: InstalledApp) : AutomationRunnerAction
    data class RemoveChip(val chipItem: ChipItem) : AutomationRunnerAction
    data class UpdateScenario(val automationScenario: AutomationScenario) : AutomationRunnerAction
    data class RunScenarioClicked(val automationScenario: AutomationScenario) : AutomationRunnerAction
    data object StopScenarioClicked : AutomationRunnerAction
}
