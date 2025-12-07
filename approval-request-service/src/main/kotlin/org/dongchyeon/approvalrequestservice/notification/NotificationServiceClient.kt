package org.dongchyeon.approvalrequestservice.notification

import org.dongchyeon.approvalrequestservice.approval.model.FinalStatus
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException

@Component
class NotificationServiceClient(
    @Value("\${notification.service.base-url}") baseUrl: String,
) {

    private val log = LoggerFactory.getLogger(NotificationServiceClient::class.java)
    private val client: RestClient = RestClient.builder()
        .baseUrl(baseUrl)
        .requestFactory(SimpleClientHttpRequestFactory().apply {
            setConnectTimeout(3000)
            setReadTimeout(5000)
        })
        .build()

    fun sendFinalStatusNotification(
        requestId: Long,
        requesterId: Long,
        finalStatus: FinalStatus,
        rejectedBy: Long?,
    ) {
        if (finalStatus == FinalStatus.IN_PROGRESS) {
            return
        }

        val payload = FinalStatusNotificationPayload(
            requesterId = requesterId,
            requestId = requestId,
            finalStatus = finalStatus.value,
            rejectedBy = rejectedBy,
        )

        try {
            client.post()
                .uri("/notifications/final-status")
                .body(payload)
                .retrieve()
                .toBodilessEntity()
        } catch (ex: RestClientException) {
            log.warn(
                "Failed to notify final status {} for request {}",
                finalStatus,
                requestId,
                ex,
            )
        }
    }

    private data class FinalStatusNotificationPayload(
        val requesterId: Long,
        val requestId: Long,
        val finalStatus: String,
        val rejectedBy: Long?,
    )
}
