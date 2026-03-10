package com.sobornov.todo_service.service

import com.sobornov.todo_service.repository.TaskRepository
import com.sobornov.todo_service.repository.model.TaskStatus
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Component
class TaskPastDueScheduler(
    private val taskRepository: TaskRepository,
) {
    @Scheduled(cron = "0 * * * * *")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    fun updatePastDueTasks() {
        val currentTimestamp = Instant.now()
        val tasks = taskRepository.findAllByStatus(TaskStatus.NOT_DONE)
        val expiredTasks = tasks
            .filter { it.deadline.isBefore(currentTimestamp) }
            .onEach { it.status = TaskStatus.PAST_DUE }
        taskRepository.saveAll(expiredTasks)
    }
}