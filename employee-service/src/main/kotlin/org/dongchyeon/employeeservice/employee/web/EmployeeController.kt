package org.dongchyeon.employeeservice.employee.web

import jakarta.validation.Valid
import org.dongchyeon.employeeservice.employee.service.EmployeeService
import org.dongchyeon.employeeservice.employee.web.dto.CreateEmployeeRequest
import org.dongchyeon.employeeservice.employee.web.dto.CreateEmployeeResponse
import org.dongchyeon.employeeservice.employee.web.dto.EmployeeResponse
import org.dongchyeon.employeeservice.employee.web.dto.UpdateEmployeeRequest
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
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

    @PutMapping("/{id}")
    fun updateEmployee(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateEmployeeRequest,
    ): EmployeeResponse {
        val updated = employeeService.updateEmployee(id, request)

        return EmployeeResponse(
            id = updated.id ?: error("Employee ID is missing"),
            name = updated.name,
            department = updated.department,
            position = updated.position,
        )
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteEmployee(
        @PathVariable id: Long,
    ) {
        employeeService.deleteEmployee(id)
    }
}
