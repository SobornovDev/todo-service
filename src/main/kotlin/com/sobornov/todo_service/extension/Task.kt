package com.sobornov.todo_service.extension

import com.sobornov.todo_service.controller.model.TaskResponse
import com.sobornov.todo_service.repository.model.Task

fun Task.toTaskResponse() =
    TaskResponse(
        this.id,
        this.description,
        this.status,
        this.createdAt,
        this.deadline,
        this.finishedAt
    )