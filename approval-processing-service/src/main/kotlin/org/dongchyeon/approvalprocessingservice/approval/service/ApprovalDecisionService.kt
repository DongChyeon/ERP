package org.dongchyeon.approvalprocessingservice.approval.service

import io.grpc.StatusRuntimeException
import org.dongchyeon.approvalprocessingservice.approval.grpc.ApprovalResultGrpcClient
import org.dongchyeon.approvalprocessingservice.approval.model.ApprovalDecisionCommand
import org.dongchyeon.approvalprocessingservice.approval.model.ApprovalResultPayload
import org.dongchyeon.approvalprocessingservice.approval.model.ApprovalStatus
import org.dongchyeon.approvalprocessingservice.approval.repository.InMemoryApprovalRequestRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class ApprovalDecisionService(
    private val repository: InMemoryApprovalRequestRepository,
    private val approvalResultGrpcClient: ApprovalResultGrpcClient,
) {

    fun decide(command: ApprovalDecisionCommand) {
        val request = repository.findByRequestId(command.requestId)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Approval request ${command.requestId} not found",
            )

        val step = request.steps.firstOrNull { it.approverId == command.approverId }
            ?: throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Approver ${command.approverId} is not assigned to request ${command.requestId}",
            )

        val updatedSteps = request.steps.map {
            if (it.step == step.step && it.approverId == command.approverId) {
                it.copy(status = command.status)
            } else {
                it
            }
        }

        repository.save(request.copy(steps = updatedSteps))

        val result = ApprovalResultPayload(
            requestId = command.requestId,
            step = step.step,
            approverId = command.approverId,
            status = command.status,
        )

        try {
            approvalResultGrpcClient.sendResult(result)
        } catch (ex: StatusRuntimeException) {
            throw ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "Failed to notify approval request service",
                ex,
            )
        }
    }

    fun parseStatus(rawStatus: String): ApprovalStatus {
        val status = ApprovalStatus.from(rawStatus)
        if (status == ApprovalStatus.PENDING) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Status must be approved or rejected",
            )
        }
        return status
    }
}
