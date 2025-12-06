package org.dongchyeon.approvalrequestservice.approval.service

import io.grpc.StatusRuntimeException
import java.time.Instant
import org.dongchyeon.approvalrequestservice.approval.common.SequenceGeneratorService
import org.dongchyeon.approvalrequestservice.approval.grpc.ApprovalProcessingGrpcClient
import org.dongchyeon.approvalrequestservice.approval.model.ApprovalRequestDocument
import org.dongchyeon.approvalrequestservice.approval.model.ApprovalRequestResponse
import org.dongchyeon.approvalrequestservice.approval.model.ApprovalResultCommand
import org.dongchyeon.approvalrequestservice.approval.model.ApprovalStep
import org.dongchyeon.approvalrequestservice.approval.model.ApprovalStatus
import org.dongchyeon.approvalrequestservice.approval.model.CreateApprovalRequest
import org.dongchyeon.approvalrequestservice.approval.model.CreateApprovalResponse
import org.dongchyeon.approvalrequestservice.approval.model.CreateApprovalStep
import org.dongchyeon.approvalrequestservice.approval.model.FinalStatus
import org.dongchyeon.approvalrequestservice.approval.repository.ApprovalRequestRepository
import org.dongchyeon.approvalrequestservice.employee.EmployeeServiceClient
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class ApprovalRequestService(
    private val repository: ApprovalRequestRepository,
    private val sequenceGeneratorService: SequenceGeneratorService,
    private val employeeServiceClient: EmployeeServiceClient,
    private val approvalProcessingGrpcClient: ApprovalProcessingGrpcClient,
) {
    fun create(request: CreateApprovalRequest): CreateApprovalResponse {
        validateStepsSequence(request.steps)
        validateEmployees(request)

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
            updatedAt = null,
        )

        sendToProcessing(document)
        repository.save(document)
        return CreateApprovalResponse(requestId)
    }

    private fun sendToProcessing(document: ApprovalRequestDocument) {
        try {
            approvalProcessingGrpcClient.forward(document)
        } catch (ex: StatusRuntimeException) {
            throw ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Approval Processing Service unavailable",
                ex,
            )
        }
    }

    private fun validateEmployees(request: CreateApprovalRequest) {
        employeeServiceClient.ensureEmployeeExists(request.requesterId)
        request.steps.forEach { step ->
            employeeServiceClient.ensureEmployeeExists(step.approverId)
        }
    }

    private fun validateStepsSequence(steps: List<CreateApprovalStep>) {
        steps.forEachIndexed { index, step ->
            val expectedStep = index + 1
            if (step.step != expectedStep) {
                throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Approval steps must start at 1 and increment by 1. Expected $expectedStep but got ${step.step}",
                )
            }
        }
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

    fun applyApprovalResult(command: ApprovalResultCommand) {
        val document = repository.findById(command.requestId)
            .orElseThrow { NoSuchElementException("Approval request ${command.requestId} not found") }
        repository.save(document.applyResult(command))
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
            updatedAt = updatedAt,
        )

    private fun ApprovalRequestDocument.applyResult(
        command: ApprovalResultCommand,
    ): ApprovalRequestDocument {
        var stepUpdated = false
        val updatedSteps = steps.map { step ->
            if (step.step == command.step && step.approverId == command.approverId) {
                stepUpdated = true
                if (step.status == command.status) step else step.copy(status = command.status)
            } else {
                step
            }
        }

        if (!stepUpdated) {
            throw IllegalArgumentException(
                "Approval step ${command.step} for approver ${command.approverId} not found",
            )
        }

        val finalStatus = when (command.status) {
            ApprovalStatus.APPROVED -> FinalStatus.APPROVED
            ApprovalStatus.REJECTED -> FinalStatus.REJECTED
            ApprovalStatus.PENDING -> throw IllegalArgumentException("Status must be approved or rejected")
        }

        return copy(
            steps = updatedSteps,
            finalStatus = finalStatus,
            updatedAt = Instant.now(),
        )
    }
}
