package org.dongchyeon.approvalprocessingservice.approval.grpc

import approval.ApprovalGrpc
import approval.ApprovalOuterClass.ApprovalRequest
import approval.ApprovalOuterClass.ApprovalResponse
import approval.ApprovalOuterClass.ApprovalResultRequest
import approval.ApprovalOuterClass.ApprovalResultResponse
import approval.ApprovalOuterClass.Step
import io.grpc.Status
import io.grpc.stub.StreamObserver
import org.dongchyeon.approvalprocessingservice.approval.model.ApprovalRequest as DomainApprovalRequest
import org.dongchyeon.approvalprocessingservice.approval.model.ApprovalStep as DomainApprovalStep
import org.dongchyeon.approvalprocessingservice.approval.model.ApprovalStatus
import org.dongchyeon.approvalprocessingservice.approval.repository.InMemoryApprovalRequestRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ApprovalGrpcService(
    private val repository: InMemoryApprovalRequestRepository,
) : ApprovalGrpc.ApprovalImplBase() {

    private val log = LoggerFactory.getLogger(ApprovalGrpcService::class.java)

    override fun requestApproval(
        request: ApprovalRequest,
        responseObserver: StreamObserver<ApprovalResponse>,
    ) {
        runCatching {
            val approvalRequest = request.toDomain()
            repository.save(approvalRequest)
            ApprovalResponse.newBuilder()
                .setStatus("received")
                .build()
        }.onSuccess { response ->
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        }.onFailure { ex ->
            log.error("Failed to store approval request {}", request.requestId, ex)
            responseObserver.onError(
                Status.INTERNAL
                    .withDescription("Failed to store approval request")
                    .withCause(ex)
                    .asRuntimeException(),
            )
        }
    }

    override fun returnApprovalResult(
        request: ApprovalResultRequest,
        responseObserver: StreamObserver<ApprovalResultResponse>,
    ) {
        log.info(
            "Received approval result for requestId={}, step={}, approverId={}, status={} (not persisted)",
            request.requestId,
            request.step,
            request.approverId,
            request.status,
        )
        val response = ApprovalResultResponse.newBuilder()
            .setStatus("received")
            .build()
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    private fun ApprovalRequest.toDomain(): DomainApprovalRequest =
        DomainApprovalRequest(
            requestId = requestId.toLong(),
            requesterId = requesterId.toLong(),
            title = title,
            content = content,
            steps = stepsList.map { it.toDomain() },
        )

    private fun Step.toDomain(): DomainApprovalStep =
        DomainApprovalStep(
            step = step,
            approverId = approverId.toLong(),
            status = ApprovalStatus.from(status),
        )
}
