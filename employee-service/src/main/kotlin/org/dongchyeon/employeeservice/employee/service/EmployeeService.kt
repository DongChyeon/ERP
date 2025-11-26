package org.dongchyeon.employeeservice.employee.service

import org.dongchyeon.employeeservice.employee.model.Employee
import org.dongchyeon.employeeservice.employee.repository.EmployeeRepository
import org.dongchyeon.employeeservice.employee.web.dto.CreateEmployeeRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class EmployeeService(
    private val employeeRepository: EmployeeRepository,
) {

    @Transactional
    fun createEmployee(request: CreateEmployeeRequest): Long {
        val saved = employeeRepository.save(
            Employee(
                name = request.name.trim(),
                department = request.department.trim(),
                position = request.position.trim(),
            ),
        )

        return saved.id ?: error("Failed to persist employee")
    }
}
