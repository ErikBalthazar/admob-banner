package com.erikbalthazar.admobbanner.common.exception

/**
 * A sealed class representing various application-specific exceptions.
 *
 * This class serves as a base for different types of exceptions that can occur
 * within the application, allowing for more specific error handling.
 */
sealed class AppException : Exception()

/**
 * Exception indicating a network-related error.
 */
class NetworkException : AppException()

/**
 * Exception indicating an unknown or unexpected error.
 */
class UnknownException : AppException()