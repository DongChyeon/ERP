package org.dongchyeon.approvalrequestservice.approval.model

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

/**
 * API payloads used for creating approval requests.
 */
data class CreateApprovalRequest(
    @field:Positive
    val requesterId: Long,
    @field:NotBlank
    val title: String,
    @field:NotBlank
    val content: String,
    @field:NotEmpty
    @field:Size(min = 1)
    val steps: List<CreateApprovalStep>,
)

data class CreateApprovalStep(
    @field:Positive
    val step: Int,
    @field:Positive
    val approverId: Long,
)

data class CreateApprovalResponse(
    val requestId: Long,
)
