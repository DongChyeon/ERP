package org.dongchyeon.notificationservice.notification

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

data class FinalStatusNotificationEvent(
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

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromValue(value: String): FinalDecisionStatus =
            entries.find { it.value == value }
                ?: throw IllegalArgumentException("Unknown status: $value")
    }

    override fun toString(): String = value
}
