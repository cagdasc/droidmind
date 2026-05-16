package com.cacaosd.droidmind.domain

interface AgentClientFactory {

    fun createRemoteModel(): List<AgentClient>

    suspend fun createLocalAgents(): List<AgentClient>
}
