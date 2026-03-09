package com.sobornov.todo_service.controller.model

import jakarta.validation.constraints.NotBlank

data class TaskUpdateDescriptionRequest(
    @field:NotBlank
    val description: String,
)
