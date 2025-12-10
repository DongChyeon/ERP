package org.dongchyeon.notificationservice.notification.messaging

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
@EnableConfigurationProperties(NotificationMessagingProperties::class)
class RabbitConfig(
    private val properties: NotificationMessagingProperties,
) {

    @Bean
    fun notificationDeclarables(): Declarables {
        val exchange = TopicExchange(properties.exchange, true, false)
        val queue = Queue(
            properties.queue,
            true,
            false,
            false,
            mapOf(
                "x-dead-letter-exchange" to dlxName(properties.exchange),
                "x-dead-letter-routing-key" to dlqRoutingKey(properties.routingKey),
            ),
        )
        val binding = BindingBuilder
            .bind(queue)
            .to(exchange)
            .with(properties.routingKey)
        val dlx = TopicExchange(dlxName(properties.exchange), true, false)
        val dlq = Queue(dlqName(properties.queue), true)
        val dlqBinding = BindingBuilder
            .bind(dlq)
            .to(dlx)
            .with(dlqRoutingKey(properties.routingKey))

        return Declarables(exchange, queue, binding, dlx, dlq, dlqBinding)
    }

    @Bean
    fun rabbitTemplate(connectionFactory: ConnectionFactory): RabbitTemplate = RabbitTemplate(connectionFactory)

    private fun dlxName(exchange: String) = "$exchange.dlx"

    private fun dlqName(queue: String) = "$queue.dlq"

    private fun dlqRoutingKey(routingKey: String) = "$routingKey.dlq"
}
