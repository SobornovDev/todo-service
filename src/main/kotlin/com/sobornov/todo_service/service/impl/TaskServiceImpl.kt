package com.sobornov.todo_service.service.impl

import com.sobornov.todo_service.controller.model.TaskCreateRequest
import com.sobornov.todo_service.controller.model.TaskResponse
import com.sobornov.todo_service.exception.InvalidStatusTransitionException
import com.sobornov.todo_service.exception.NotFoundException
import com.sobornov.todo_service.repository.TaskRepository
import com.sobornov.todo_service.repository.model.Task
import com.sobornov.todo_service.repository.model.TaskStatus
import com.sobornov.todo_service.service.TaskService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class TaskServiceImpl(
    private val repository: TaskRepository
) : TaskService {

    override fun create(request: TaskCreateRequest): TaskResponse {
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

    override fun updateDescription(requestId: Long, description: String): TaskResponse {
        val task = repository.findByIdOrNull(requestId)
        task?.let {
            it.description = description
        } ?: throw NotFoundException("Task with id: $requestId not found")
        repository.save(task)
        return TaskResponse(
            task.id,
            task.description,
            task.status,
            task.createdAt,
            task.deadline,
            task.finishedAt
        )
    }

    override fun updateStatus(
        requestId: Long,
        status: TaskStatus
    ): TaskResponse {
        val task = repository.findByIdOrNull(requestId)
        task?.let {
            if (it.status.isTransitionAvailable(status)) {
                it.status = status
            } else throw InvalidStatusTransitionException("Invalid status transition: from ${it.status} to $status")
        } ?: throw NotFoundException("Task with id:$requestId not found")
        repository.save(task)
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