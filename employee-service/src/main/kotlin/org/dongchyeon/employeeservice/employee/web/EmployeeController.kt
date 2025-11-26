package org.dongchyeon.employeeservice.employee.web

import jakarta.validation.Valid
import org.dongchyeon.employeeservice.employee.service.EmployeeService
import org.dongchyeon.employeeservice.employee.web.dto.CreateEmployeeRequest
import org.dongchyeon.employeeservice.employee.web.dto.CreateEmployeeResponse
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/employees")
@Validated
class EmployeeController(
    private val employeeService: EmployeeService,
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createEmployee(
        @Valid @RequestBody request: CreateEmployeeRequest,
    ): CreateEmployeeResponse {
        val id = employeeService.createEmployee(request)
        return CreateEmployeeResponse(id)
    }
}
