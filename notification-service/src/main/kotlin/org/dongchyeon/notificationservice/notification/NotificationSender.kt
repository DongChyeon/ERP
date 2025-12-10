package org.dongchyeon.notificationservice.notification

import com.fasterxml.jackson.databind.ObjectMapper
import org.dongchyeon.notificationservice.websocket.WebSocketSessionRegistry
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.socket.TextMessage

@Service
class NotificationSender(
    private val sessionRegistry: WebSocketSessionRegistry,
    private val objectMapper: ObjectMapper,
) {

    private val log = LoggerFactory.getLogger(NotificationSender::class.java)

    fun sendFinalStatus(event: FinalStatusNotificationEvent) {
        val session = sessionRegistry.sessionFor(event.requesterId)
        if (session == null || !session.isOpen) {
            log.info(
                "Requester {} has no active WebSocket session. Skipping final status notification for request {}",
                event.requesterId,
                event.requestId,
            )
            return
        }

        val message = FinalStatusNotificationMessage(
            requestId = event.requestId,
            finalStatus = event.finalStatus,
            rejectedBy = event.rejectedBy,
        )
        val payload = objectMapper.writeValueAsString(message)

        runCatching {
            session.sendMessage(TextMessage(payload))
        }.onFailure { ex ->
            log.warn(
                "Failed to deliver notification for request {} to requester {}",
                event.requestId,
                event.requesterId,
                ex,
            )
        }
    }
}
