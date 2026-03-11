package com.sobornov.todo_service.service

import com.sobornov.todo_service.repository.TaskRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Component
class TaskPastDueScheduler(
    private val taskRepository: TaskRepository,
) {
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    fun updatePastDueTasks() {
        val currentTimestamp = Instant.now()
        taskRepository.markPastDue(currentTimestamp)
    }
}