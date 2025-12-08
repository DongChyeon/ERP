package org.dongchyeon.approvalprocessingservice.approval.messaging

import com.google.gson.Gson
import org.dongchyeon.approvalprocessingservice.approval.model.ApprovalResultPayload
import org.dongchyeon.common.messaging.ApprovalResultMessage
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component

@Component
class ApprovalResultPublisher(
    private val rabbitTemplate: RabbitTemplate,
    private val properties: ApprovalMessagingProperties,
    private val gson: Gson,
) {

    private val log = LoggerFactory.getLogger(ApprovalResultPublisher::class.java)

    fun publish(payload: ApprovalResultPayload) {
        val message = ApprovalResultMessage(
            requestId = payload.requestId,
            step = payload.step,
            approverId = payload.approverId,
            status = payload.status,
        )
        val jsonPayload = gson.toJson(message)
        rabbitTemplate.convertAndSend(properties.resultExchange, properties.resultRoutingKey, jsonPayload)
        log.debug(
            "Published approval result for request {}, step {}, approver {}",
            payload.requestId,
            payload.step,
            payload.approverId,
        )
    }
}
