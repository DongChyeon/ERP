package org.dongchyeon.approvalprocessingservice.approval.model

import org.dongchyeon.common.messaging.ApprovalStatus

/**
 * Approval Request Service의 ApprovalRequestDocument에서 finalStatus와 createdAt 필드 제거
 */
data class ApprovalRequest(
    val requestId: Long,
    val requesterId: Long,
    val title: String,
    val content: String,
    val steps: List<ApprovalStep>,
)

data class ApprovalStep(
    val step: Int,
    val approverId: Long,
    val status: ApprovalStatus,
)
