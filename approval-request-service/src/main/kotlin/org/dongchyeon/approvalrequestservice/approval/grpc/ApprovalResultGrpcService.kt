package org.dongchyeon.approvalrequestservice.approval.grpc

import approval.ApprovalGrpc
import approval.ApprovalOuterClass.ApprovalResultRequest
import approval.ApprovalOuterClass.ApprovalResultResponse
import io.grpc.Status
import io.grpc.stub.StreamObserver
import org.dongchyeon.approvalrequestservice.approval.model.ApprovalResultCommand
import org.dongchyeon.approvalrequestservice.approval.model.ApprovalStatus
import org.dongchyeon.approvalrequestservice.approval.service.ApprovalRequestService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ApprovalResultGrpcService(
    private val approvalRequestService: ApprovalRequestService,
) : ApprovalGrpc.ApprovalImplBase() {

    private val log = LoggerFactory.getLogger(ApprovalResultGrpcService::class.java)

    override fun returnApprovalResult(
        request: ApprovalResultRequest,
        responseObserver: StreamObserver<ApprovalResultResponse>,
    ) {
        runCatching {
            val command = request.toCommand()
            approvalRequestService.applyApprovalResult(command)
            ApprovalResultResponse.newBuilder()
                .setStatus("updated")
                .build()
        }.onSuccess { response ->
            log.info(
                "Updated approval request {} step {} by approver {} to {}",
                request.requestId,
                request.step,
                request.approverId,
                request.status,
            )
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        }.onFailure { ex ->
            val status = when (ex) {
                is NoSuchElementException -> Status.NOT_FOUND.withDescription(ex.message)
                is IllegalArgumentException -> Status.INVALID_ARGUMENT.withDescription(ex.message)
                else -> Status.INTERNAL.withDescription("Failed to update approval result")
            }.withCause(ex)
            log.error(
                "Failed to update approval request {} step {} by approver {}",
                request.requestId,
                request.step,
                request.approverId,
                ex,
            )
            responseObserver.onError(status.asRuntimeException())
        }
    }

    private fun ApprovalResultRequest.toCommand(): ApprovalResultCommand {
        val status = ApprovalStatus.from(this.status)
        if (status == ApprovalStatus.PENDING) {
            throw IllegalArgumentException("Status must be approved or rejected")
        }
        return ApprovalResultCommand(
            requestId = requestId.toLong(),
            step = step,
            approverId = approverId.toLong(),
            status = status,
        )
    }
}
