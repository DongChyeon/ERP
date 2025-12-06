package org.dongchyeon.approvalrequestservice.approval.grpc

import approval.ApprovalGrpc
import approval.ApprovalOuterClass
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import jakarta.annotation.PreDestroy
import java.util.concurrent.TimeUnit
import org.dongchyeon.approvalrequestservice.approval.model.ApprovalRequestDocument
import org.dongchyeon.approvalrequestservice.approval.model.ApprovalStep
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class ApprovalProcessingGrpcClient(
    @Value("\${approval.processing.grpc.host:localhost}") private val host: String,
    @Value("\${approval.processing.grpc.port:50051}") private val port: Int,
) {

    private val channel: ManagedChannel = ManagedChannelBuilder
        .forAddress(host, port)
        .usePlaintext()
        .build()
    private val blockingStub: ApprovalGrpc.ApprovalBlockingStub = ApprovalGrpc.newBlockingStub(channel)

    fun forward(document: ApprovalRequestDocument) {
        val request = document.toProto()
        blockingStub.requestApproval(request)
    }

    @PreDestroy
    fun shutdown() {
        channel.shutdown()
        try {
            channel.awaitTermination(5, TimeUnit.SECONDS)
        } catch (ex: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }

    private fun ApprovalRequestDocument.toProto(): ApprovalOuterClass.ApprovalRequest {
        val stepsProto = steps.map { it.toProto() }
        return ApprovalOuterClass.ApprovalRequest.newBuilder()
            .setRequestId(requestId)
            .setRequesterId(requesterId)
            .setTitle(title)
            .setContent(content)
            .addAllSteps(stepsProto)
            .build()
    }

    private fun ApprovalStep.toProto(): ApprovalOuterClass.Step =
        ApprovalOuterClass.Step.newBuilder()
            .setStep(step)
            .setApproverId(approverId)
            .setStatus(status.value)
            .build()
}
