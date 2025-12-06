package org.dongchyeon.approvalprocessingservice.approval.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

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

enum class ApprovalStatus(@get:JsonValue val value: String) {
    PENDING("pending"),
    APPROVED("approved"),
    REJECTED("rejected");

    companion object {
        @JvmStatic
        @JsonCreator
        fun from(value: String): ApprovalStatus =
            entries.firstOrNull { it.value == value.lowercase() }
                ?: throw IllegalArgumentException("Unknown approval status: $value")
    }
}
