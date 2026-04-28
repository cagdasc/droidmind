package com.cacaosd.droidmind.domain.local.scenario

import java.time.Instant
import java.util.*

data class ScenarioModel(
    val id: UUID,
    val title: String,
    val prompt: String,
    val shortDescription: String,
    val timestamp: Instant
)
