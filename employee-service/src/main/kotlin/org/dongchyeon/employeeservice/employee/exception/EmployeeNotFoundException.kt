package org.dongchyeon.employeeservice.employee.exception

class EmployeeNotFoundException(id: Long) : RuntimeException("Employee with ID $id not found")
