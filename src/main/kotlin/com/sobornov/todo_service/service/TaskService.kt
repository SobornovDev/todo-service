package com.sobornov.todo_service.service

import com.sobornov.todo_service.controller.model.TaskCreateRequest
import com.sobornov.todo_service.controller.model.TaskResponse
import com.sobornov.todo_service.repository.TaskRepository
import com.sobornov.todo_service.repository.model.Task
import com.sobornov.todo_service.repository.model.TaskStatus
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class TaskService(
    private val repository: TaskRepository
) {
    fun create(request: TaskCreateRequest): TaskResponse {
        val task = repository.save(
            Task(
                description = request.description,
                deadline = request.deadline,
                status = TaskStatus.NOT_DONE,
                createdAt = Instant.now()
            )
        )
        return TaskResponse(
            task.id,
            task.description,
            task.status,
            task.createdAt,
            task.deadline,
            task.finishedAt
        )
    }
}