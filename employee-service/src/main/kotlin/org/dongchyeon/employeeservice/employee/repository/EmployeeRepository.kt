package org.dongchyeon.employeeservice.employee.repository

import org.dongchyeon.employeeservice.employee.model.Employee
import org.springframework.data.jpa.repository.JpaRepository

interface EmployeeRepository : JpaRepository<Employee, Long> {

    fun findAllByDepartmentAndPosition(department: String, position: String): List<Employee>

    fun findAllByDepartment(department: String): List<Employee>

    fun findAllByPosition(position: String): List<Employee>
}
