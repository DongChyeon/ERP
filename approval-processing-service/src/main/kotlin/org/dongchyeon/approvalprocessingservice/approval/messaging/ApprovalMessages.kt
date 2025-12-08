package org.dongchyeon.approvalprocessingservice.approval.messaging

import org.dongchyeon.approvalprocessingservice.approval.model.ApprovalStatus

data class ApprovalRequestMessage(
    val requestId: Long,
    val requesterId: Long,
    val title: String,
    val content: String,
    val steps: List<ApprovalRequestStepMessage>,
)

data class ApprovalRequestStepMessage(
    val step: Int,
    val approverId: Long,
    val status: ApprovalStatus,
)

data class ApprovalResultMessage(
    val requestId: Long,
    val step: Int,
    val approverId: Long,
    val status: ApprovalStatus,
)
