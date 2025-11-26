package org.dongchyeon.employeeservice.employee.service

import org.dongchyeon.employeeservice.employee.model.Employee
import org.dongchyeon.employeeservice.employee.repository.EmployeeRepository
import org.dongchyeon.employeeservice.employee.web.dto.CreateEmployeeRequest
import org.dongchyeon.employeeservice.employee.web.dto.UpdateEmployeeRequest
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

    @Transactional(readOnly = true)
    fun findEmployees(department: String?, position: String?): List<Employee> {
        return when {
            department.isNullOrBlank() && position.isNullOrBlank() -> employeeRepository.findAll()
            department.isNullOrBlank() -> employeeRepository.findAllByPosition(position!!.trim())
            position.isNullOrBlank() -> employeeRepository.findAllByDepartment(department.trim())
            else -> employeeRepository.findAllByDepartmentAndPosition(
                department.trim(),
                position.trim(),
            )
        }
    }

    @Transactional(readOnly = true)
    fun findEmployeeById(id: Long): Employee? {
        return employeeRepository.findById(id).orElse(null)
    }

    @Transactional
    fun updateEmployee(id: Long, request: UpdateEmployeeRequest): Employee {
        val existing = employeeRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Employee with ID $id not found") }

        val updated = existing.copy(
            department = request.department.trim(),
            position = request.position.trim(),
        )

        return employeeRepository.save(updated)
    }
}
