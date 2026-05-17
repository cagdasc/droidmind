package com.cacaosd.droidmind.domain.result

import kotlinx.coroutines.flow.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

fun <Data> getAsFlow(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    block: suspend () -> Data
): Flow<Data> {
    return flow { emit(block()) }.flowOn(coroutineContext)
}

fun <Data> getAsFlowResult(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    block: suspend () -> Data
): Flow<Result<Data>> {
    return getAsFlow(coroutineContext = coroutineContext) { block() }.toResult()
}

suspend fun <Data> getAsResult(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    block: suspend () -> Data
): Result<Data> {
    return getAsFlowResult(coroutineContext = coroutineContext) { block() }.first()
}

suspend fun <Data> Flow<Data>.asResult(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
): Result<Data> {
    return asResultFlow(coroutineContext).first()
}

fun <Data> Flow<Data>.asResultFlow(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
): Flow<Result<Data>> {
    return toResult().flowOn(coroutineContext)
}

fun <Data> Flow<Data>.toResult(): Flow<Result<Data>> {
    return map { Result.success(it) }
        .catch { throwable ->
            emit(Result.failure(throwable))
        }
}

inline fun <T, R> Result<T>.mapResult(success: (T?) -> R, failure: (Throwable?) -> R): R {
    return when {
        this.isSuccess -> success(this.getOrNull())
        else -> failure(this.exceptionOrNull())
    }
}

inline fun <T, R> Result<T>.mapSuccess(transform: (T) -> Result<R>): Result<R> =
    if (isSuccess) {
        val value = getOrNull()
        if (value != null) transform(value)
        else Result.failure(IllegalStateException("Success value was null"))
    } else Result.failure(exceptionOrNull() ?: Exception("Unknown failure"))

inline fun <T> Result<T>.mapFailure(transform: (Throwable?) -> Result<T>): Result<T> {
    return when {
        this.isSuccess -> this
        else -> transform(this.exceptionOrNull())
    }
}

fun <T> Result<T>.requireValue(): T {
    return getOrElse {
        throw IllegalArgumentException()
    }
}

inline fun <T1, T2, R> Result<T1>.combine(result2: Result<T2>, transform: (T1, T2) -> R): Result<R> {
    return when {
        this.isSuccess && result2.isSuccess -> Result.success(transform(this.getOrNull()!!, result2.getOrNull()!!))
        else -> Result.failure(this.exceptionOrNull() ?: result2.exceptionOrNull()!!)
    }
}
