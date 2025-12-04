package org.dongchyeon.approvalrequestservice.approval.service

import java.time.Instant
import org.dongchyeon.approvalrequestservice.approval.common.SequenceGeneratorService
import org.dongchyeon.approvalrequestservice.approval.model.ApprovalRequestDocument
import org.dongchyeon.approvalrequestservice.approval.model.ApprovalRequestResponse
import org.dongchyeon.approvalrequestservice.approval.model.ApprovalStep
import org.dongchyeon.approvalrequestservice.approval.model.ApprovalStatus
import org.dongchyeon.approvalrequestservice.approval.model.CreateApprovalRequest
import org.dongchyeon.approvalrequestservice.approval.model.CreateApprovalResponse
import org.dongchyeon.approvalrequestservice.approval.model.FinalStatus
import org.dongchyeon.approvalrequestservice.approval.repository.ApprovalRequestRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class ApprovalRequestService(
    private val repository: ApprovalRequestRepository,
    private val sequenceGeneratorService: SequenceGeneratorService,
) {
    fun create(request: CreateApprovalRequest): CreateApprovalResponse {
        val requestId = sequenceGeneratorService.generateApprovalRequestId()
        val steps = request.steps.map { step ->
            ApprovalStep(
                step = step.step,
                approverId = step.approverId,
                status = ApprovalStatus.PENDING,
            )
        }

        val document = ApprovalRequestDocument(
            requestId = requestId,
            requesterId = request.requesterId,
            title = request.title,
            content = request.content,
            steps = steps,
            finalStatus = FinalStatus.IN_PROGRESS,
            createdAt = Instant.now(),
        )

        repository.save(document)
        return CreateApprovalResponse(requestId)
    }

    fun getApprovalRequests(): List<ApprovalRequestResponse> =
        repository.findAll().map { it.toResponse() }

    fun getApprovalRequest(requestId: Long): ApprovalRequestResponse =
        repository.findById(requestId)
            .map { it.toResponse() }
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Approval request $requestId not found",
                )
            }

    private fun ApprovalRequestDocument.toResponse(): ApprovalRequestResponse =
        ApprovalRequestResponse(
            requestId = requestId,
            requesterId = requesterId,
            title = title,
            content = content,
            steps = steps,
            finalStatus = finalStatus,
            createdAt = createdAt,
        )
}
