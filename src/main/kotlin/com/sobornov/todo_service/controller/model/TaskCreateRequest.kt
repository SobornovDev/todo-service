package com.sobornov.todo_service.controller.model

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.Instant

data class TaskCreateRequest(
    @field:NotBlank
    val description: String,

    @field:NotNull
    var deadline: Instant
)
