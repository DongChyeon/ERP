package org.dongchyeon.employeeservice.employee.web

import jakarta.validation.Valid
import org.dongchyeon.employeeservice.employee.service.EmployeeService
import org.dongchyeon.employeeservice.employee.web.dto.CreateEmployeeRequest
import org.dongchyeon.employeeservice.employee.web.dto.CreateEmployeeResponse
import org.dongchyeon.employeeservice.employee.web.dto.EmployeeResponse
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/employees")
@Validated
class EmployeeController(
    private val employeeService: EmployeeService,
) {

    @GetMapping
    fun getEmployees(
        @RequestParam(required = false) department: String?,
        @RequestParam(required = false) position: String?,
    ): List<EmployeeResponse> {
        return employeeService.findEmployees(department, position)
            .map { employee ->
                EmployeeResponse(
                    id = employee.id ?: error("Employee ID is missing"),
                    name = employee.name,
                    department = employee.department,
                    position = employee.position,
                )
            }
    }

    @GetMapping("/{id}")
    fun getEmployeeById(
        @PathVariable id: Long,
    ): EmployeeResponse {
        val employee = employeeService.findEmployeeById(id)
            ?: throw IllegalArgumentException("Employee with ID $id not found")

        return EmployeeResponse(
            id = employee.id ?: error("Employee ID is missing"),
            name = employee.name,
            department = employee.department,
            position = employee.position,
        )
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createEmployee(
        @Valid @RequestBody request: CreateEmployeeRequest,
    ): CreateEmployeeResponse {
        val id = employeeService.createEmployee(request)
        return CreateEmployeeResponse(id)
    }
}
