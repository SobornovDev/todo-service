package com.sobornov.todo_service.controller.model

import com.sobornov.todo_service.repository.model.TaskStatus
import java.time.Instant

data class TaskResponse(
    val id: Long,
    val description: String,
    val status: TaskStatus,
    val createdAt: Instant,
    val deadline: Instant,
    val finishedAt: Instant?
)
