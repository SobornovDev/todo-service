package com.sobornov.todo_service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class TodoServiceApplication

fun main(args: Array<String>) {
	runApplication<TodoServiceApplication>(*args)
}
