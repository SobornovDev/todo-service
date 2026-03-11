package com.sobornov.todo_service.controller

import com.sobornov.todo_service.repository.TaskRepository
import com.sobornov.todo_service.repository.model.Task
import com.sobornov.todo_service.repository.model.TaskStatus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant
import java.time.temporal.ChronoUnit

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var taskRepository: TaskRepository

    @BeforeEach
    fun setUp() {
        taskRepository.deleteAll()
    }


    @Test
    fun `should create task and return 201 with location header`() {
        mockMvc.perform(
            post("/api/v1/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "description": "Blumen kaufen",
                        "deadline": "2027-01-01T00:00:00Z"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isCreated)
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.status").value("NOT_DONE"))
            .andExpect(jsonPath("$.description").value("Blumen kaufen"))
    }

    @Test
    fun `should return 400 when description is blank`() {
        mockMvc.perform(
            post("/api/v1/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{ "description": "", "deadline": "2027-01-01T00:00:00Z" }""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").exists())
    }

    @Test
    fun `should return 400 when deadline is in the past`() {
        mockMvc.perform(
            post("/api/v1/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{ "description": "Old task", "deadline": "2000-01-01T00:00:00Z" }""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").exists())
    }


    @Test
    fun `should return task by id`() {
        val task = savedTask("Read a book")

        mockMvc.perform(get("/api/v1/tasks/${task.id}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(task.id))
            .andExpect(jsonPath("$.description").value("Read a book"))
            .andExpect(jsonPath("$.status").value("NOT_DONE"))
    }

    @Test
    fun `should return 404 when task not found`() {
        mockMvc.perform(get("/api/v1/tasks/999"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").exists())
    }


    @Test
    fun `should return all tasks when no status filter`() {
        savedTask("Task 1")
        savedTask("Task 2", status = TaskStatus.DONE)

        mockMvc.perform(get("/api/v1/tasks"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
    }

    @Test
    fun `should return empty list when no tasks`() {
        mockMvc.perform(get("/api/v1/tasks"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(0))
    }

    @Test
    fun `should return tasks filtered by status`() {
        savedTask("Active task", status = TaskStatus.NOT_DONE)
        savedTask("Done task", status = TaskStatus.DONE)

        mockMvc.perform(get("/api/v1/tasks").param("status", "NOT_DONE"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].status").value("NOT_DONE"))
    }

    @Test
    fun `should return 400 when status filter is invalid`() {
        mockMvc.perform(get("/api/v1/tasks").param("status", "UNKNOWN"))
            .andExpect(status().isBadRequest)
    }


    @Test
    fun `should update task description`() {
        val task = savedTask("Old description")

        mockMvc.perform(
            patch("/api/v1/tasks/${task.id}/description")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{ "description": "New description" }""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.description").value("New description"))
    }

    @Test
    fun `should return 404 when updating description of non-existent task`() {
        mockMvc.perform(
            patch("/api/v1/tasks/999/description")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{ "description": "New description" }""")
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").exists())
    }

    @Test
    fun `should return 400 when new description is blank`() {
        val task = savedTask("Some task")

        mockMvc.perform(
            patch("/api/v1/tasks/${task.id}/description")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{ "description": "" }""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").exists())
    }

    @Test
    fun `should return 400 when updating description of past due task`() {
        val task = savedTask(
            "Overdue",
            status = TaskStatus.PAST_DUE,
            deadline = Instant.now().minusSeconds(3600)
        )

        mockMvc.perform(
            patch("/api/v1/tasks/${task.id}/description")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{ "description": "Updated" }""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").exists())
    }


    @Test
    fun `should update task status from NOT_DONE to DONE`() {
        val task = savedTask("Finish report")

        mockMvc.perform(
            patch("/api/v1/tasks/${task.id}/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{ "status": "DONE" }""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("DONE"))
            .andExpect(jsonPath("$.finishedAt").exists())
    }

    @Test
    fun `should update task status from DONE to NOT_DONE`() {
        val task = savedTask("Reopened task", status = TaskStatus.DONE)

        mockMvc.perform(
            patch("/api/v1/tasks/${task.id}/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{ "status": "NOT_DONE" }""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("NOT_DONE"))
    }

    @Test
    fun `should return 409 when transitioning from PAST_DUE`() {
        val task = savedTask(
            "Overdue task",
            status = TaskStatus.PAST_DUE,
            deadline = Instant.now().minusSeconds(3600)
        )

        mockMvc.perform(
            patch("/api/v1/tasks/${task.id}/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{ "status": "DONE" }""")
        )
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.message").exists())
    }

    @Test
    fun `should return 404 when updating status of non-existent task`() {
        mockMvc.perform(
            patch("/api/v1/tasks/999/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{ "status": "DONE" }""")
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").exists())
    }


    private fun savedTask(
        description: String,
        status: TaskStatus = TaskStatus.NOT_DONE,
        deadline: Instant = Instant.now().plus(30, ChronoUnit.DAYS)
    ): Task = taskRepository.save(
        Task(
            description = description,
            status = status,
            createdAt = Instant.now(),
            deadline = deadline
        )
    )
}