package com.sobornov.todo_service.repository.model

enum class TaskStatus {
    NOT_DONE,
    DONE,
    PAST_DUE;

    fun isTransitionAvailable(target: TaskStatus): Boolean {
        return when (this) {
            NOT_DONE -> target == DONE || target == PAST_DUE

            DONE -> target == NOT_DONE

            PAST_DUE -> false
        }
    }
}