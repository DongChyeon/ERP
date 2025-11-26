package org.dongchyeon.employeeservice.employee.web.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UpdateEmployeeRequest(
    @field:NotBlank
    @field:Size(max = 100)
    val department: String,

    @field:NotBlank
    @field:Size(max = 100)
    val position: String,
)
