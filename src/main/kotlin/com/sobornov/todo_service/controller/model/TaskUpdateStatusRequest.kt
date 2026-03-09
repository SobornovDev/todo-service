package com.sobornov.todo_service.controller.model

import com.sobornov.todo_service.repository.model.TaskStatus
import jakarta.validation.constraints.NotNull

data class TaskUpdateStatusRequest(
    @field:NotNull
    var status: TaskStatus
)
