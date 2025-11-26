package org.dongchyeon.employeeservice.common.web

import jakarta.servlet.http.HttpServletRequest
import org.dongchyeon.employeeservice.employee.exception.EmployeeNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.Instant

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(EmployeeNotFoundException::class)
    fun handleEmployeeNotFound(
        ex: EmployeeNotFoundException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> = buildResponse(HttpStatus.NOT_FOUND, ex.message, request)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val fieldError = ex.bindingResult.fieldErrors.firstOrNull()
        val message = fieldError?.let { "${it.field} ${it.defaultMessage}" } ?: "Validation failed"
        return buildResponse(HttpStatus.BAD_REQUEST, message, request)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(
        ex: IllegalArgumentException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> = buildResponse(HttpStatus.BAD_REQUEST, ex.message, request)

    @ExceptionHandler(Exception::class)
    fun handleGeneric(
        ex: Exception,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> = buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.message, request)

    private fun buildResponse(
        status: HttpStatus,
        message: String?,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val body = ErrorResponse(
            timestamp = Instant.now(),
            status = status.value(),
            error = status.reasonPhrase,
            message = message ?: status.reasonPhrase,
            path = request.requestURI,
        )
        return ResponseEntity.status(status).body(body)
    }
}
