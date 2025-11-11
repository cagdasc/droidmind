package com.cacaosd.droidmind.domain.session

interface ScenarioExecutor {
    suspend fun execute(request: ScenarioExecution) {
        execute(
            deviceSerial = request.deviceSerial,
            packageName = request.packageName,
            prompt = request.scenario
        )
    }

    suspend fun execute(deviceSerial: String?, packageName: String, prompt: String)
}