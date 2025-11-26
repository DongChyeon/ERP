package org.dongchyeon.employeeservice.employee.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "employees")
data class Employee(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, length = 100)
    val name: String,

    @Column(nullable = false, length = 100)
    val department: String,

    @Column(nullable = false, length = 100)
    val position: String,

    @Column(name = "created_at", updatable = false, insertable = false)
    val createdAt: LocalDateTime? = null,
)
