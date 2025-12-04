package org.dongchyeon.approvalrequestservice.employee

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import org.springframework.web.server.ResponseStatusException

@Component
class EmployeeServiceClient(
    @Value("\${employee.service.base-url}") baseUrl: String,
) {
    private val log = LoggerFactory.getLogger(EmployeeServiceClient::class.java)
    private val client: RestClient = RestClient.builder()
        .baseUrl(baseUrl)
        .build()

    fun ensureEmployeeExists(employeeId: Long) {
        try {
            client.get()
                .uri("/employees/{id}", employeeId)
                .retrieve()
                .toBodilessEntity()
        } catch (ex: HttpClientErrorException.NotFound) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Employee $employeeId not found",
                ex,
            )
        } catch (ex: HttpClientErrorException) {
            log.warn("Employee service returned {} for id {}", ex.statusCode.value(), employeeId)
            throw ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "Failed to verify employee $employeeId",
                ex,
            )
        } catch (ex: RestClientException) {
            log.warn("Unable to call employee service for id {}", employeeId, ex)
            throw ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Employee service is unavailable",
                ex,
            )
        }
    }
}
