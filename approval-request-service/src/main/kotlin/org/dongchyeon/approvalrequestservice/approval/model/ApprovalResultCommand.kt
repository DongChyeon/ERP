package org.dongchyeon.approvalrequestservice.approval.model

import org.dongchyeon.common.messaging.ApprovalStatus

data class ApprovalResultCommand(
    val requestId: Long,
    val step: Int,
    val approverId: Long,
    val status: ApprovalStatus,
)
