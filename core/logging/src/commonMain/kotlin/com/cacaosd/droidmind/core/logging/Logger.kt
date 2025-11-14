package com.cacaosd.droidmind.core.logging

import io.github.oshai.kotlinlogging.KotlinLogging

object Logger {
    private val logger = KotlinLogging.logger("DroidMind")

    fun debug(message: String, payload: Map<String, Any> = emptyMap()) {
        logger.atDebug {
            this.message = message
            this.payload = payload
        }
    }

    fun info(message: String, payload: Map<String, Any> = emptyMap()) {
        logger.atInfo {
            this.message = message
            this.payload = payload
        }
    }

    fun error(message: String, throwable: Throwable, payload: Map<String, Any> = emptyMap()) {
        logger.atError {
            this.message = message
            this.cause = throwable
            this.payload = payload
        }
    }
}