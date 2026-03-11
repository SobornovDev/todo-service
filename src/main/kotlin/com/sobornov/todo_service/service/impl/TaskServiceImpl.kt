package com.sobornov.todo_service.service.impl

import com.sobornov.todo_service.controller.model.TaskCreateRequest
import com.sobornov.todo_service.controller.model.TaskResponse
import com.sobornov.todo_service.exception.InvalidStatusTransitionException
import com.sobornov.todo_service.exception.NotFoundException
import com.sobornov.todo_service.extension.toTaskResponse
import com.sobornov.todo_service.repository.TaskRepository
import com.sobornov.todo_service.repository.model.Task
import com.sobornov.todo_service.repository.model.TaskStatus
import com.sobornov.todo_service.service.TaskService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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
        return task.toTaskResponse()
    }

    @Transactional(readOnly = true)
    override fun getTaskById(taskId: Long): TaskResponse {
        val task = repository.findByIdOrNull(taskId)
            ?: throw NotFoundException("Task with id: $taskId not found")
        return task.toTaskResponse()
    }

    @Transactional(readOnly = true)
    override fun getAllByStatus(status: TaskStatus?): List<TaskResponse> {
        val tasks = status?.let {
            repository.findAllByStatus(status)
        } ?: repository.findAll()
        return tasks.map { it.toTaskResponse() }
    }

    @Transactional
    override fun updateDescription(requestId: Long, description: String): TaskResponse {
        val task = repository.findByIdOrNull(requestId)
        task?.let {
            if (it.status == TaskStatus.PAST_DUE) {
                throw IllegalStateException("Task with id: $requestId is past due and cannot be modified")
            }
            it.description = description
        } ?: throw NotFoundException("Task with id: $requestId not found")
        repository.save(task)
        return task.toTaskResponse()
    }

    @Transactional
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

        if (status == TaskStatus.DONE) {
            task.finishedAt = Instant.now()
        }
        repository.save(task)
        return task.toTaskResponse()
    }
}