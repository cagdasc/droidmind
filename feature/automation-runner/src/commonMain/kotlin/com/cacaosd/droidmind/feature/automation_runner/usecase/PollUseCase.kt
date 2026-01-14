package com.cacaosd.droidmind.feature.automation_runner.usecase

import com.cacaosd.droidmind.core.logging.Logger
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow

class PollUseCase {
    @OptIn(ObsoleteCoroutinesApi::class)
    fun <T> poll(interval: Long = 5000L, emit: suspend () -> T): Flow<T> {
        return flow {
            val tickerChannel = ticker(delayMillis = interval, initialDelayMillis = 0L)
            val tickerFlow = tickerChannel.receiveAsFlow()
            tickerFlow
                .catch { error -> Logger.error("Error while polling", error) }
                .collect {
                val value = emit()
                emit(value)
            }
        }.catch { error ->
            Logger.error("Error while polling", error)
        }
    }
}
