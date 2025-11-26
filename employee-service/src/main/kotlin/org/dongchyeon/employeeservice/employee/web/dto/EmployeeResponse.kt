package org.dongchyeon.employeeservice.employee.web.dto

data class EmployeeResponse(
    val id: Long,
    val name: String,
    val department: String,
    val position: String,
)
