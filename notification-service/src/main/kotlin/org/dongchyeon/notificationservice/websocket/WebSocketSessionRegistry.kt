package org.dongchyeon.notificationservice.websocket

import java.util.concurrent.ConcurrentHashMap
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.WebSocketSession

@Component
class WebSocketSessionRegistry {
    private val log = LoggerFactory.getLogger(WebSocketSessionRegistry::class.java)
    private val sessions: MutableMap<Long, WebSocketSession> = ConcurrentHashMap()

    fun register(employeeId: Long, session: WebSocketSession) {
        sessions.put(employeeId, session)?.let { previous ->
            if (previous.isOpen) {
                runCatching { previous.close(CloseStatus.NORMAL) }
            }
        }
        log.info("Registered WebSocket session {} for employee {}", session.id, employeeId)
    }

    fun remove(employeeId: Long, sessionId: String?) {
        sessions.computeIfPresent(employeeId) { _, existing ->
            if (sessionId == null || existing.id == sessionId) {
                log.info("Removing WebSocket session {} for employee {}", existing.id, employeeId)
                null
            } else {
                existing
            }
        }
    }

    fun sessionFor(employeeId: Long): WebSocketSession? = sessions[employeeId]
}
