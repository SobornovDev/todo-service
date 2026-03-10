package com.sobornov.todo_service.repository

import com.sobornov.todo_service.repository.model.Task
import com.sobornov.todo_service.repository.model.TaskStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TaskRepository : JpaRepository<Task, Long> {
    fun findAllByStatus(status: TaskStatus): List<Task>
}