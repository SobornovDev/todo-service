package com.sobornov.todo_service.controller

import com.sobornov.todo_service.controller.model.TaskCreateRequest
import com.sobornov.todo_service.controller.model.TaskResponse
import com.sobornov.todo_service.controller.model.TaskUpdateDescriptionRequest
import com.sobornov.todo_service.controller.model.TaskUpdateStatusRequest
import com.sobornov.todo_service.service.TaskService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@RestController
@RequestMapping("api/v1/tasks")
class TaskController(
    private val taskService: TaskService
) {

    @PostMapping
    fun createTask(@RequestBody @Valid request: TaskCreateRequest): ResponseEntity<TaskResponse> {
        val created = taskService.create(request)
        val location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.id)
            .toUri()
        return ResponseEntity.created(location).body(created)
    }

    @PatchMapping("/{requestId}/description")
    fun updateDescription(
        @PathVariable requestId: Long,
        @RequestBody @Valid request: TaskUpdateDescriptionRequest
    ): ResponseEntity<TaskResponse> {
        return ResponseEntity.ok(taskService.updateDescription(requestId, request.description))
    }

    @PatchMapping("/{requestId}/status")
    fun updateStatus(
        @PathVariable requestId: Long,
        @RequestBody @Valid request: TaskUpdateStatusRequest
    ): ResponseEntity<TaskResponse> {
        return ResponseEntity.ok(taskService.updateStatus(requestId, request.status))
    }
}