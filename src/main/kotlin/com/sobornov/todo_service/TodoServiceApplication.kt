package com.sobornov.todo_service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TodoServiceApplication

fun main(args: Array<String>) {
	runApplication<TodoServiceApplication>(*args)
}
