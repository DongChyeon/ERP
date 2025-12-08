package org.dongchyeon.approvalprocessingservice.approval.messaging

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.annotation.EnableRabbit
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Qualifier
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
    fun approvalRequestExchange(): TopicExchange =
        TopicExchange(properties.requestExchange, true, false)

    @Bean
    fun approvalRequestQueue(): Queue = Queue(properties.requestQueue, true)

    @Bean
    fun approvalRequestBinding(
        approvalRequestQueue: Queue,
        @Qualifier("approvalRequestExchange") approvalRequestExchange: TopicExchange,
    ): Binding =
        BindingBuilder
            .bind(approvalRequestQueue)
            .to(approvalRequestExchange)
            .with(properties.requestRoutingKey)

    @Bean
    fun approvalResultExchange(): TopicExchange =
        TopicExchange(properties.resultExchange, true, false)

    @Bean
    fun rabbitTemplate(
        connectionFactory: ConnectionFactory,
    ): RabbitTemplate = RabbitTemplate(connectionFactory)
}
