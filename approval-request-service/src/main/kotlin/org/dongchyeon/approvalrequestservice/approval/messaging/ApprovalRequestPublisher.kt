package org.dongchyeon.approvalrequestservice.approval.messaging

import com.google.gson.Gson
import org.dongchyeon.approvalrequestservice.approval.model.ApprovalRequestDocument
import org.dongchyeon.approvalrequestservice.approval.model.ApprovalStep
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component

@Component
class ApprovalRequestPublisher(
    private val rabbitTemplate: RabbitTemplate,
    private val properties: ApprovalMessagingProperties,
    private val gson: Gson,
) {

    private val log = LoggerFactory.getLogger(ApprovalRequestPublisher::class.java)

    fun publish(document: ApprovalRequestDocument) {
        val message = document.toMessage()
        val jsonPayload = gson.toJson(message)
        rabbitTemplate.convertAndSend(properties.requestExchange, properties.requestRoutingKey, jsonPayload)
        log.debug(
            "Published approval request {} for requester {} to exchange {}",
            document.requestId,
            document.requesterId,
            properties.requestExchange,
        )
    }

    private fun ApprovalRequestDocument.toMessage(): ApprovalRequestMessage =
        ApprovalRequestMessage(
            requestId = requestId,
            requesterId = requesterId,
            title = title,
            content = content,
            steps = steps.map { it.toMessage() },
        )

    private fun ApprovalStep.toMessage(): ApprovalRequestStepMessage =
        ApprovalRequestStepMessage(
            step = step,
            approverId = approverId,
            status = status,
        )
}
