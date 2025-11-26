package org.dongchyeon.employeeservice.employee.web.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * Represents the minimal input required to create an employee.
 */
data class CreateEmployeeRequest(
    @field:NotBlank
    @field:Size(max = 100)
    val name: String,
    @field:NotBlank
    @field:Size(max = 100)
    val position: String,
    @field:NotBlank
    @field:Size(max = 100)
    val department: String,
)
