package dev.alllexey.itmoqueue.bot

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Failure(val msg: String) : ValidationResult()
}