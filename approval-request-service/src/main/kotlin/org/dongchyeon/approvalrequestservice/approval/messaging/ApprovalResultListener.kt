package org.dongchyeon.approvalrequestservice.approval.messaging

import com.google.gson.Gson
import org.dongchyeon.approvalrequestservice.approval.model.ApprovalResultCommand
import org.dongchyeon.approvalrequestservice.approval.service.ApprovalRequestService
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class ApprovalResultListener(
    private val approvalRequestService: ApprovalRequestService,
    private val gson: Gson,
) {

    private val log = LoggerFactory.getLogger(ApprovalResultListener::class.java)

    @RabbitListener(queues = ["\${approval.messaging.result.queue}"])
    fun handle(payload: String) {
        val message = gson.fromJson(payload, ApprovalResultMessage::class.java)
        log.debug(
            "Received approval result for request {}, step {}, approver {}",
            message.requestId,
            message.step,
            message.approverId,
        )
        approvalRequestService.applyApprovalResult(
            ApprovalResultCommand(
                requestId = message.requestId,
                step = message.step,
                approverId = message.approverId,
                status = message.status,
            ),
        )
    }
}
