package com.adsmaker.app.core

/**
 * Tiny result wrapper used across the data layer so callers can handle
 * success and failure without try/catch noise. [message] on failure is
 * always user-presentable — never a raw stack trace.
 */
sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Failure(val message: String, val cause: Throwable? = null) : AppResult<Nothing>
}

inline fun <T> AppResult<T>.onSuccess(block: (T) -> Unit): AppResult<T> {
    if (this is AppResult.Success) block(data)
    return this
}

inline fun <T> AppResult<T>.onFailure(block: (String, Throwable?) -> Unit): AppResult<T> {
    if (this is AppResult.Failure) block(message, cause)
    return this
}
