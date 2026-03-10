package com.sobornov.todo_service.service

import com.sobornov.todo_service.controller.model.TaskCreateRequest
import com.sobornov.todo_service.controller.model.TaskResponse
import com.sobornov.todo_service.repository.model.TaskStatus

interface TaskService {
    fun create(request: TaskCreateRequest): TaskResponse

    fun getTaskById(taskId: Long): TaskResponse

    fun getAllByStatus(status: TaskStatus?): List<TaskResponse>

    fun updateDescription(requestId: Long, description: String): TaskResponse

    fun updateStatus(requestId: Long, status: TaskStatus): TaskResponse
}