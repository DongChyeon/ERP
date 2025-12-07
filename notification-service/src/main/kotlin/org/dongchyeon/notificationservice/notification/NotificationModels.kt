package org.dongchyeon.notificationservice.notification

import com.fasterxml.jackson.annotation.JsonValue

data class FinalStatusNotificationRequest(
    val requesterId: Long,
    val requestId: Long,
    val finalStatus: FinalDecisionStatus,
    val rejectedBy: Long?,
)

data class FinalStatusNotificationMessage(
    val type: String = "final-status",
    val requestId: Long,
    val finalStatus: FinalDecisionStatus,
    val rejectedBy: Long?,
)

enum class FinalDecisionStatus(@JsonValue private val value: String) {
    APPROVED("approved"),
    REJECTED("rejected");

    override fun toString(): String = value
}
