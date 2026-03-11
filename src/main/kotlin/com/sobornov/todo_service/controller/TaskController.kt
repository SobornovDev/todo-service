package com.sobornov.todo_service.controller

import com.sobornov.todo_service.controller.model.ErrorResponse
import com.sobornov.todo_service.controller.model.TaskCreateRequest
import com.sobornov.todo_service.controller.model.TaskResponse
import com.sobornov.todo_service.controller.model.TaskUpdateDescriptionRequest
import com.sobornov.todo_service.controller.model.TaskUpdateStatusRequest
import com.sobornov.todo_service.repository.model.TaskStatus
import com.sobornov.todo_service.service.TaskService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@RestController
@RequestMapping("api/v1/tasks")
@Tag(name = "Tasks", description = "Todo list management")
class TaskController(
    private val taskService: TaskService
) {

    @PostMapping
    @Operation(summary = "Create a new task")
    @ApiResponse(responseCode = "201", description = "Task created")
    @ApiResponse(
        responseCode = "400", description = "Invalid request body", content = [Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class)
        )]
    )
    fun createTask(@RequestBody @Valid request: TaskCreateRequest): ResponseEntity<TaskResponse> {
        val created = taskService.create(request)
        val location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.id)
            .toUri()
        return ResponseEntity.created(location).body(created)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task by id")
    @ApiResponse(responseCode = "200", description = "Task found")
    @ApiResponse(
        responseCode = "404", description = "Task not found", content = [Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class)
        )]
    )
    fun getTaskById(@PathVariable id: Long): ResponseEntity<TaskResponse> {
        return ResponseEntity.ok(taskService.getTaskById(id))
    }

    @GetMapping
    @Operation(
        summary = "Get tasks",
        description = "Returns all tasks. Filter by status optionally."
    )
    fun getAllByStatus(@RequestParam status: TaskStatus?): ResponseEntity<List<TaskResponse>> {
        return ResponseEntity.ok(taskService.getAllByStatus(status))
    }

    @PatchMapping("/{requestId}/description")
    @Operation(summary = "Update task description")
    @ApiResponse(responseCode = "200", description = "Description updated")
    @ApiResponse(
        responseCode = "404", description = "Task not found", content = [Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class)
        )]
    )
    @ApiResponse(
        responseCode = "409", description = "Task is past due", content = [Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class)
        )]
    )
    fun updateDescription(
        @PathVariable requestId: Long,
        @RequestBody @Valid request: TaskUpdateDescriptionRequest
    ): ResponseEntity<TaskResponse> {
        return ResponseEntity.ok(taskService.updateDescription(requestId, request.description))
    }

    @PatchMapping("/{requestId}/status")
    @Operation(summary = "Update task status")
    @ApiResponse(responseCode = "200", description = "Status updated")
    @ApiResponse(
        responseCode = "404", description = "Task not found", content = [Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class)
        )]
    )
    @ApiResponse(
        responseCode = "409", description = "Transition not allowed", content = [Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class)
        )]
    )
    fun updateStatus(
        @PathVariable requestId: Long,
        @RequestBody @Valid request: TaskUpdateStatusRequest
    ): ResponseEntity<TaskResponse> {
        return ResponseEntity.ok(taskService.updateStatus(requestId, request.status))
    }
}