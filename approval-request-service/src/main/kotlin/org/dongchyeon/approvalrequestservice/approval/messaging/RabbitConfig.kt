package org.dongchyeon.approvalrequestservice.approval.messaging

import org.dongchyeon.approvalrequestservice.notification.NotificationMessagingProperties
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Declarables
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.annotation.EnableRabbit
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableRabbit
@EnableConfigurationProperties(value = [ApprovalMessagingProperties::class, NotificationMessagingProperties::class])
class RabbitConfig(
    private val approvalProperties: ApprovalMessagingProperties,
    private val notificationProperties: NotificationMessagingProperties,
) {

    @Bean
    fun approvalMessagingDeclarables(): Declarables {
        val requestExchange = TopicExchange(approvalProperties.requestExchange, true, false)
        val requestQueue = Queue(
            approvalProperties.requestQueue,
            true,
            false,
            false,
            mapOf(
                "x-dead-letter-exchange" to dlxName(approvalProperties.requestExchange),
                "x-dead-letter-routing-key" to dlqRoutingKey(approvalProperties.requestRoutingKey),
            ),
        )
        val requestBinding = BindingBuilder
            .bind(requestQueue)
            .to(requestExchange)
            .with(approvalProperties.requestRoutingKey)
        val requestDlx = TopicExchange(dlxName(approvalProperties.requestExchange), true, false)
        val requestDlq = Queue(dlqName(approvalProperties.requestQueue), true)
        val requestDlqBinding = BindingBuilder
            .bind(requestDlq)
            .to(requestDlx)
            .with(dlqRoutingKey(approvalProperties.requestRoutingKey))

        val resultExchange = TopicExchange(approvalProperties.resultExchange, true, false)
        val resultQueue = Queue(
            approvalProperties.resultQueue,
            true,
            false,
            false,
            mapOf(
                "x-dead-letter-exchange" to dlxName(approvalProperties.resultExchange),
                "x-dead-letter-routing-key" to dlqRoutingKey(approvalProperties.resultRoutingKey),
            ),
        )
        val resultBinding = BindingBuilder
            .bind(resultQueue)
            .to(resultExchange)
            .with(approvalProperties.resultRoutingKey)
        val resultDlx = TopicExchange(dlxName(approvalProperties.resultExchange), true, false)
        val resultDlq = Queue(dlqName(approvalProperties.resultQueue), true)
        val resultDlqBinding = BindingBuilder
            .bind(resultDlq)
            .to(resultDlx)
            .with(dlqRoutingKey(approvalProperties.resultRoutingKey))

        val notificationExchange = TopicExchange(notificationProperties.exchange, true, false)
        val notificationQueue = Queue(
            notificationProperties.queue,
            true,
            false,
            false,
            mapOf(
                "x-dead-letter-exchange" to dlxName(notificationProperties.exchange),
                "x-dead-letter-routing-key" to dlqRoutingKey(notificationProperties.routingKey),
            ),
        )
        val notificationBinding = BindingBuilder
            .bind(notificationQueue)
            .to(notificationExchange)
            .with(notificationProperties.routingKey)
        val notificationDlx = TopicExchange(dlxName(notificationProperties.exchange), true, false)
        val notificationDlq = Queue(dlqName(notificationProperties.queue), true)
        val notificationDlqBinding = BindingBuilder
            .bind(notificationDlq)
            .to(notificationDlx)
            .with(dlqRoutingKey(notificationProperties.routingKey))

        return Declarables(
            requestExchange,
            requestQueue,
            requestBinding,
            requestDlx,
            requestDlq,
            requestDlqBinding,
            resultExchange,
            resultQueue,
            resultBinding,
            resultDlx,
            resultDlq,
            resultDlqBinding,
            notificationExchange,
            notificationQueue,
            notificationBinding,
            notificationDlx,
            notificationDlq,
            notificationDlqBinding,
        )
    }

    @Bean
    fun rabbitTemplate(
        connectionFactory: ConnectionFactory,
    ): RabbitTemplate = RabbitTemplate(connectionFactory)

    private fun dlxName(exchange: String) = "$exchange.dlx"

    private fun dlqName(queue: String) = "$queue.dlq"

    private fun dlqRoutingKey(routingKey: String) = "$routingKey.dlq"
}
