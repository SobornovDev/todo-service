package com.sobornov.todo_service.service

import com.sobornov.todo_service.controller.model.TaskCreateRequest
import com.sobornov.todo_service.exception.InvalidStatusTransitionException
import com.sobornov.todo_service.exception.NotFoundException
import com.sobornov.todo_service.repository.TaskRepository
import com.sobornov.todo_service.repository.model.Task
import com.sobornov.todo_service.repository.model.TaskStatus
import com.sobornov.todo_service.service.impl.TaskServiceImpl
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.repository.findByIdOrNull
import java.time.Instant
import java.time.temporal.ChronoUnit

class TaskServiceImplTest {

    private val repository = mockk<TaskRepository>()
    private val service = TaskServiceImpl(repository)


    @Test
    fun `create saves task with NOT_DONE status and returns response`() {
        val deadline = Instant.now().plus(7, ChronoUnit.DAYS)
        val request = TaskCreateRequest(description = "Buy flowers", deadline = deadline)
        val savedTask = task(description = "Buy flowers", deadline = deadline)
        val slot = slot<Task>()

        every { repository.save(capture(slot)) } returns savedTask

        val result = service.create(request)

        assertEquals("Buy flowers", slot.captured.description)
        assertEquals(TaskStatus.NOT_DONE, slot.captured.status)
        assertEquals(deadline, slot.captured.deadline)
        assertEquals("Buy flowers", result.description)
        assertEquals(TaskStatus.NOT_DONE, result.status)
    }

    @Test
    fun `getTaskById returns response when task exists`() {
        val existing = task(id = 1L, description = "Read a book")
        every { repository.findByIdOrNull(1L) } returns existing

        val result = service.getTaskById(1L)

        assertEquals(1L, result.id)
        assertEquals("Read a book", result.description)
    }

    @Test
    fun `getTaskById throws NotFoundException when task missing`() {
        every { repository.findByIdOrNull(99L) } returns null

        assertThrows<NotFoundException> { service.getTaskById(99L) }
    }

    @Test
    fun `getAllByStatus returns all tasks when status is null`() {
        val tasks = listOf(task(id = 1L), task(id = 2L, status = TaskStatus.DONE))
        every { repository.findAll() } returns tasks

        val result = service.getAllByStatus(null)

        assertEquals(2, result.size)
        verify(exactly = 0) { repository.findAllByStatus(any()) }
    }

    @Test
    fun `getAllByStatus returns filtered tasks when status provided`() {
        val tasks = listOf(task(id = 1L, status = TaskStatus.NOT_DONE))
        every { repository.findAllByStatus(TaskStatus.NOT_DONE) } returns tasks

        val result = service.getAllByStatus(TaskStatus.NOT_DONE)

        assertEquals(1, result.size)
        assertEquals(TaskStatus.NOT_DONE, result[0].status)
        verify(exactly = 0) { repository.findAll() }
    }

    @Test
    fun `getAllByStatus returns empty list when no tasks match`() {
        every { repository.findAllByStatus(TaskStatus.DONE) } returns emptyList()

        val result = service.getAllByStatus(TaskStatus.DONE)

        assertEquals(0, result.size)
    }

    @Test
    fun `updateDescription updates and saves task`() {
        val existing = task(id = 1L, description = "Old")
        every { repository.findByIdOrNull(1L) } returns existing
        every { repository.save(existing) } returns existing

        val result = service.updateDescription(1L, "New")

        assertEquals("New", result.description)
        verify { repository.save(existing) }
    }

    @Test
    fun `updateDescription throws NotFoundException when task missing`() {
        every { repository.findByIdOrNull(99L) } returns null

        assertThrows<NotFoundException> { service.updateDescription(99L, "New") }
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `updateDescription throws IllegalStateException when task is PAST_DUE`() {
        val pastDue = task(id = 1L, status = TaskStatus.PAST_DUE)
        every { repository.findByIdOrNull(1L) } returns pastDue

        assertThrows<IllegalStateException> { service.updateDescription(1L, "New") }
        verify(exactly = 0) { repository.save(any()) }
    }


    @Test
    fun `updateStatus transitions NOT_DONE to DONE and sets finishedAt`() {
        val existing = task(id = 1L, status = TaskStatus.NOT_DONE)
        every { repository.findByIdOrNull(1L) } returns existing
        every { repository.save(existing) } returns existing

        val result = service.updateStatus(1L, TaskStatus.DONE)

        assertEquals(TaskStatus.DONE, result.status)
        assertNotNull(result.finishedAt)
    }

    @Test
    fun `updateStatus transitions DONE to NOT_DONE and does not set finishedAt`() {
        val existing = task(id = 1L, status = TaskStatus.DONE)
        every { repository.findByIdOrNull(1L) } returns existing
        every { repository.save(existing) } returns existing

        val result = service.updateStatus(1L, TaskStatus.NOT_DONE)

        assertEquals(TaskStatus.NOT_DONE, result.status)
        assertNull(result.finishedAt)
    }

    @Test
    fun `updateStatus throws InvalidStatusTransitionException for PAST_DUE to DONE`() {
        val pastDue = task(id = 1L, status = TaskStatus.PAST_DUE)
        every { repository.findByIdOrNull(1L) } returns pastDue

        assertThrows<InvalidStatusTransitionException> {
            service.updateStatus(1L, TaskStatus.DONE)
        }
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `updateStatus throws InvalidStatusTransitionException for PAST_DUE to NOT_DONE`() {
        val pastDue = task(id = 1L, status = TaskStatus.PAST_DUE)
        every { repository.findByIdOrNull(1L) } returns pastDue

        assertThrows<InvalidStatusTransitionException> {
            service.updateStatus(1L, TaskStatus.NOT_DONE)
        }
    }

    @Test
    fun `updateStatus throws NotFoundException when task missing`() {
        every { repository.findByIdOrNull(99L) } returns null

        assertThrows<NotFoundException> { service.updateStatus(99L, TaskStatus.DONE) }
        verify(exactly = 0) { repository.save(any()) }
    }

    private fun task(
        id: Long = 1L,
        description: String = "Test task",
        status: TaskStatus = TaskStatus.NOT_DONE,
        deadline: Instant = Instant.now().plus(7, ChronoUnit.DAYS),
        finishedAt: Instant? = null
    ) = Task(
        id = id,
        description = description,
        status = status,
        createdAt = Instant.now(),
        deadline = deadline,
        finishedAt = finishedAt
    )
}
