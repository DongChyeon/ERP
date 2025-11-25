package org.dongchyeon.employeeservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class EmployeeServiceApplication

fun main(args: Array<String>) {
    runApplication<EmployeeServiceApplication>(*args)
}
