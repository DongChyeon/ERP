package org.dongchyeon.notificationservice.notification.messaging

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.dongchyeon.notificationservice.notification.FinalStatusNotificationEvent
import org.dongchyeon.notificationservice.notification.NotificationSender
import org.slf4j.LoggerFactory
import org.springframework.amqp.AmqpRejectAndDontRequeueException
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class FinalStatusNotificationListener(
    private val notificationSender: NotificationSender,
    private val objectMapper: ObjectMapper,
) {

    private val log = LoggerFactory.getLogger(FinalStatusNotificationListener::class.java)

    @RabbitListener(queues = ["\${notification.messaging.queue}"])
    fun handle(payload: String) {
        val event = try {
            objectMapper.readValue(payload, FinalStatusNotificationEvent::class.java)
        } catch (ex: JsonProcessingException) {
            log.error("Failed to parse final status notification payload: {}", payload, ex)
            throw AmqpRejectAndDontRequeueException("Invalid notification payload", ex)
        }
        notificationSender.sendFinalStatus(event)
    }
}
