package com.sobornov.todo_service.controller

import com.sobornov.todo_service.controller.model.ErrorResponse
import com.sobornov.todo_service.exception.InvalidStatusTransitionException
import com.sobornov.todo_service.exception.NotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class RestExceptionControllerAdvice : ResponseEntityExceptionHandler() {

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(ex: NotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity<ErrorResponse>(
            ErrorResponse(ex.message),
            HttpStatus.NOT_FOUND
        )
    }

    @ExceptionHandler(InvalidStatusTransitionException::class)
    fun handleNotFound(ex: InvalidStatusTransitionException): ResponseEntity<ErrorResponse> {
        return ResponseEntity<ErrorResponse>(
            ErrorResponse(ex.message),
            HttpStatus.CONFLICT
        )
    }
}