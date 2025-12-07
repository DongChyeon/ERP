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

    fun sendFinalStatus(request: FinalStatusNotificationRequest) {
        val session = sessionRegistry.sessionFor(request.requesterId)
        if (session == null || !session.isOpen) {
            log.info(
                "Requester {} has no active WebSocket session. Skipping final status notification for request {}",
                request.requesterId,
                request.requestId,
            )
            return
        }

        val message = FinalStatusNotificationMessage(
            requestId = request.requestId,
            finalStatus = request.finalStatus,
            rejectedBy = request.rejectedBy,
        )
        val payload = objectMapper.writeValueAsString(message)

        runCatching {
            session.sendMessage(TextMessage(payload))
        }.onFailure { ex ->
            log.warn(
                "Failed to deliver notification for request {} to requester {}",
                request.requestId,
                request.requesterId,
                ex,
            )
        }
    }
}
