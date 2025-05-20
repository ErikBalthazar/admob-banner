package com.erikbalthazar.admobbanner.utils

/**
 * A sealed class representing the different states of an operation that can either be loading,
 * successful with data, or failed with an error.
 *
 * @param T The type of data that is expected upon success.
 */
sealed class Status<out T> {
    data object Loading : Status<Nothing>()
    data class Success<T>(val data: T) : Status<T>()
    data class Error(val throwable: Throwable? = null) : Status<Nothing>()
}
