package org.dongchyeon.approvalprocessingservice.approval.model

import jakarta.validation.constraints.NotBlank
import org.dongchyeon.common.messaging.ApprovalStatus

data class ProcessApprovalResultRequest(
    @field:NotBlank
    val status: String,
)

data class ProcessApprovalResultResponse(
    val status: String,
)

data class ApprovalDecisionCommand(
    val requestId: Long,
    val approverId: Long,
    val status: ApprovalStatus,
)

data class ApprovalResultPayload(
    val requestId: Long,
    val step: Int,
    val approverId: Long,
    val status: ApprovalStatus,
)
