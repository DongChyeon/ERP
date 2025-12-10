package org.dongchyeon.approvalrequestservice.notification

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "notification.messaging")
data class NotificationMessagingProperties(
    val exchange: String = "notification.final.exchange",
    val routingKey: String = "notification.final",
    val queue: String = "notification.final.queue",
)
