package org.dongchyeon.approvalrequestservice.approval.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document("approval_requests")
data class ApprovalRequestDocument(
    @Id
    val requestId: Long,
    val requesterId: Long,
    val title: String,
    val content: String,
    val steps: List<ApprovalStep>,
    val finalStatus: FinalStatus,
    val createdAt: Instant,
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

enum class FinalStatus(@get:JsonValue val value: String) {
    IN_PROGRESS("in_progress"),
    APPROVED("approved"),
    REJECTED("rejected");

    companion object {
        @JvmStatic
        @JsonCreator
        fun from(value: String): FinalStatus =
            entries.firstOrNull { it.value == value.lowercase() }
                ?: throw IllegalArgumentException("Unknown final status: $value")
    }
}
