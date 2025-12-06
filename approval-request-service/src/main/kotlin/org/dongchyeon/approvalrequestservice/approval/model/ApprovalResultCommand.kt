package org.dongchyeon.approvalrequestservice.approval.model

data class ApprovalResultCommand(
    val requestId: Long,
    val step: Int,
    val approverId: Long,
    val status: ApprovalStatus,
)
