package org.dongchyeon.approvalprocessingservice.approval.messaging

import com.google.gson.Gson
import org.dongchyeon.approvalprocessingservice.approval.model.ApprovalRequest
import org.dongchyeon.approvalprocessingservice.approval.model.ApprovalStep
import org.dongchyeon.approvalprocessingservice.approval.repository.InMemoryApprovalRequestRepository
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class ApprovalRequestListener(
    private val repository: InMemoryApprovalRequestRepository,
    private val gson: Gson,
) {

    private val log = LoggerFactory.getLogger(ApprovalRequestListener::class.java)

    @RabbitListener(queues = ["\${approval.messaging.request.queue}"])
    fun handle(payload: String) {
        val message = gson.fromJson(payload, ApprovalRequestMessage::class.java)
        val request = message.toDomain()
        repository.save(request)
        log.debug(
            "Stored approval request {} for requester {} with {} steps",
            request.requestId,
            request.requesterId,
            request.steps.size,
        )
    }

    private fun ApprovalRequestMessage.toDomain(): ApprovalRequest =
        ApprovalRequest(
            requestId = requestId,
            requesterId = requesterId,
            title = title,
            content = content,
            steps = steps.map { it.toDomain() },
        )

    private fun ApprovalRequestStepMessage.toDomain(): ApprovalStep =
        ApprovalStep(
            step = step,
            approverId = approverId,
            status = status,
        )
}
