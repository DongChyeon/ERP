package org.dongchyeon.approvalrequestservice.notification

import com.google.gson.Gson
import org.slf4j.LoggerFactory
import org.springframework.amqp.AmqpException
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component

@Component
class NotificationPublisher(
    private val rabbitTemplate: RabbitTemplate,
    private val gson: Gson,
    private val properties: NotificationMessagingProperties,
) {
    private val log = LoggerFactory.getLogger(NotificationPublisher::class.java)

    fun sendFinalStatus(command: FinalStatusNotificationCommand) {
        val payload = gson.toJson(command)
        try {
            rabbitTemplate.convertAndSend(properties.exchange, properties.routingKey, payload)
        } catch (ex: AmqpException) {
            log.warn(
                "Failed to publish final status notification for request {}",
                command.requestId,
                ex,
            )
        }
    }
}

data class FinalStatusNotificationCommand(
    val requesterId: Long,
    val requestId: Long,
    val finalStatus: String,
    val rejectedBy: Long?,
)
