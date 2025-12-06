package org.dongchyeon.approvalprocessingservice.approval.grpc

import approval.ApprovalGrpc
import approval.ApprovalOuterClass
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import jakarta.annotation.PreDestroy
import java.util.concurrent.TimeUnit
import org.dongchyeon.approvalprocessingservice.approval.model.ApprovalResultPayload
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class ApprovalResultGrpcClient(
    @Value("\${approval.result.grpc.host:localhost}") private val host: String,
    @Value("\${approval.result.grpc.port:50052}") private val port: Int,
) {

    private val channel: ManagedChannel = ManagedChannelBuilder
        .forAddress(host, port)
        .usePlaintext()
        .build()
    private val blockingStub: ApprovalGrpc.ApprovalBlockingStub = ApprovalGrpc.newBlockingStub(channel)

    fun sendResult(result: ApprovalResultPayload) {
        val request = ApprovalOuterClass.ApprovalResultRequest.newBuilder()
            .setRequestId(result.requestId.toInt())
            .setStep(result.step)
            .setApproverId(result.approverId.toInt())
            .setStatus(result.status.value)
            .build()
        blockingStub
            .withDeadlineAfter(10, TimeUnit.SECONDS)
            .returnApprovalResult(request)
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
}
