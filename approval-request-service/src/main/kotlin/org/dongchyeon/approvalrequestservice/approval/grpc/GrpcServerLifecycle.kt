package org.dongchyeon.approvalrequestservice.approval.grpc

import io.grpc.Server
import io.grpc.ServerBuilder
import jakarta.annotation.PreDestroy
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.SmartLifecycle
import org.springframework.stereotype.Component

@Component
class GrpcServerLifecycle(
    private val approvalResultGrpcService: ApprovalResultGrpcService,
    @Value("\${approval.result.grpc.port:50052}") private val port: Int,
) : SmartLifecycle {

    private val log = LoggerFactory.getLogger(GrpcServerLifecycle::class.java)
    private val running = AtomicBoolean(false)
    private var server: Server? = null

    override fun start() {
        if (!running.compareAndSet(false, true)) {
            return
        }

        server = ServerBuilder.forPort(port)
            .addService(approvalResultGrpcService)
            .build()
            .start()
        log.info("gRPC Approval result server started on port {}", port)
    }

    override fun stop() {
        if (!running.compareAndSet(true, false)) {
            return
        }

        shutdownServer()
    }

    override fun stop(callback: Runnable) {
        stop()
        callback.run()
    }

    override fun isRunning(): Boolean = running.get()

    @PreDestroy
    fun destroy() {
        stop()
    }

    private fun shutdownServer() {
        try {
            server?.shutdown()?.awaitTermination(5, TimeUnit.SECONDS)
            log.info("gRPC Approval result server stopped")
        } catch (ex: InterruptedException) {
            Thread.currentThread().interrupt()
            log.warn("Interrupted while stopping gRPC server", ex)
        }
    }
}
