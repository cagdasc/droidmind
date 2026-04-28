package com.cacaosd.droidmind.data_local.mapper

import com.cacaosd.droidmind.data_local.appdatabase.scenario.ScenarioDto
import com.cacaosd.droidmind.domain.local.scenario.ScenarioModel

class ScenarioMapper {

    fun toDomain(scenarioDto: ScenarioDto): ScenarioModel {
        return ScenarioModel(
            id = scenarioDto.id,
            title = scenarioDto.title,
            prompt = scenarioDto.prompt,
            shortDescription = scenarioDto.shortDescription,
            timestamp = scenarioDto.timestamp,
        )
    }

    fun toDomain(scenarioDtos: List<ScenarioDto>): List<ScenarioModel> = scenarioDtos.map { toDomain(it) }

    fun toDto(scenarioModel: ScenarioModel): ScenarioDto {
        return ScenarioDto(
            id = scenarioModel.id,
            title = scenarioModel.title,
            prompt = scenarioModel.prompt,
            shortDescription = scenarioModel.shortDescription,
            timestamp = scenarioModel.timestamp,
        )
    }
}
