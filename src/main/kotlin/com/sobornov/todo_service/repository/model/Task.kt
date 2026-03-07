package com.sobornov.todo_service.repository.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "tasks")
class Task(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var description: String,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: TaskStatus,

    @Column(nullable = false)
    var createdAt: Instant,

    @Column(nullable = false)
    var deadline: Instant,

    @Column
    var finishedAt: Instant? = null
)