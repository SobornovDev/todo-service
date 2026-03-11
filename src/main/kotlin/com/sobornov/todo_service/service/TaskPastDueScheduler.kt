package com.sobornov.todo_service.service

import com.sobornov.todo_service.repository.TaskRepository
import com.sobornov.todo_service.repository.model.TaskStatus
import org.slf4j.LoggerFactory
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Component
class TaskPastDueScheduler(
    private val taskRepository: TaskRepository,
) {
    companion object {
        private val log = LoggerFactory.getLogger(TaskPastDueScheduler::class.java)
    }

    @Scheduled(cron = "0 * * * * *")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    fun updatePastDueTasks() {
        val currentTimestamp = Instant.now()
        val tasks = taskRepository.findAllByStatus(TaskStatus.NOT_DONE)
        tasks
            .filter { it.deadline.isBefore(currentTimestamp) }
            .forEach { task ->
                try {
                    task.status = TaskStatus.PAST_DUE
                    taskRepository.save(task)
                } catch (_: ObjectOptimisticLockingFailureException) {
                    log.debug("Task ${task.id} was concurrently updated, skipping past due transition")
                }
            }
    }
}