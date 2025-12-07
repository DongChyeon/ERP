package org.dongchyeon.notificationservice.websocket

import java.net.URI
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import org.springframework.web.util.UriComponentsBuilder

@Component
class EmployeeNotificationWebSocketHandler(
    private val sessionRegistry: WebSocketSessionRegistry,
) : TextWebSocketHandler() {

    private val log = LoggerFactory.getLogger(EmployeeNotificationWebSocketHandler::class.java)

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val employeeId = session.employeeId()
        if (employeeId == null) {
            log.warn("Rejecting WebSocket connection {} due to missing employee id", session.id)
            session.close(CloseStatus.BAD_DATA)
            return
        }

        session.attributes[EMPLOYEE_ID_ATTRIBUTE] = employeeId
        sessionRegistry.register(employeeId, session)
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        log.debug("Ignoring client message {} from session {}", message.payload, session.id)
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        val employeeId = session.attributes[EMPLOYEE_ID_ATTRIBUTE] as? Long ?: return
        sessionRegistry.remove(employeeId, session.id)
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        val employeeId = session.attributes[EMPLOYEE_ID_ATTRIBUTE] as? Long
        log.warn("WebSocket transport error for session {} employee {}", session.id, employeeId, exception)
        if (employeeId != null) {
            sessionRegistry.remove(employeeId, session.id)
        }
    }

    private fun WebSocketSession.employeeId(): Long? {
        val uri: URI = this.uri ?: return null
        val params = UriComponentsBuilder.fromUri(uri).build().queryParams
        return params.getFirst("id")?.toLongOrNull()
    }

    companion object {
        private const val EMPLOYEE_ID_ATTRIBUTE = "employeeId"
    }
}
