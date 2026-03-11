package com.sobornov.todo_service.repository

import com.sobornov.todo_service.repository.model.Task
import com.sobornov.todo_service.repository.model.TaskStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface TaskRepository : JpaRepository<Task, Long> {
    fun findAllByStatus(status: TaskStatus): List<Task>

    @Modifying
    @Query("UPDATE Task t SET t.status = 'PAST_DUE' WHERE t.status = 'NOT_DONE' AND t.deadline < :now")
    fun markPastDue(@Param("now") now: Instant)

}