package com.sobornov.todo_service.controller.model

import java.time.Instant

data class ErrorResponse(
    val message: String?,
    val timestamp: Instant = Instant.now()
)
