package org.dongchyeon.approvalrequestservice.approval.messaging

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
@EnableConfigurationProperties(ApprovalMessagingProperties::class)
class RabbitConfig(
    private val properties: ApprovalMessagingProperties,
) {

    @Bean
    fun approvalMessagingDeclarables(): Declarables {
        val requestExchange = TopicExchange(properties.requestExchange, true, false)
        val requestQueue = Queue(
            properties.requestQueue,
            true,
            false,
            false,
            mapOf(
                "x-dead-letter-exchange" to dlxName(properties.requestExchange),
                "x-dead-letter-routing-key" to dlqRoutingKey(properties.requestRoutingKey),
            ),
        )
        val requestBinding = BindingBuilder
            .bind(requestQueue)
            .to(requestExchange)
            .with(properties.requestRoutingKey)
        val requestDlx = TopicExchange(dlxName(properties.requestExchange), true, false)
        val requestDlq = Queue(dlqName(properties.requestQueue), true)
        val requestDlqBinding = BindingBuilder
            .bind(requestDlq)
            .to(requestDlx)
            .with(dlqRoutingKey(properties.requestRoutingKey))

        val resultExchange = TopicExchange(properties.resultExchange, true, false)
        val resultQueue = Queue(
            properties.resultQueue,
            true,
            false,
            false,
            mapOf(
                "x-dead-letter-exchange" to dlxName(properties.resultExchange),
                "x-dead-letter-routing-key" to dlqRoutingKey(properties.resultRoutingKey),
            ),
        )
        val resultBinding = BindingBuilder
            .bind(resultQueue)
            .to(resultExchange)
            .with(properties.resultRoutingKey)
        val resultDlx = TopicExchange(dlxName(properties.resultExchange), true, false)
        val resultDlq = Queue(dlqName(properties.resultQueue), true)
        val resultDlqBinding = BindingBuilder
            .bind(resultDlq)
            .to(resultDlx)
            .with(dlqRoutingKey(properties.resultRoutingKey))

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
