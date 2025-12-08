package org.dongchyeon.approvalprocessingservice.approval.repository

import org.dongchyeon.approvalprocessingservice.approval.model.ApprovalRequest
import org.dongchyeon.common.messaging.ApprovalStatus
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

@Repository
class InMemoryApprovalRequestRepository {
    private val requestsById: MutableMap<Long, ApprovalRequest> = ConcurrentHashMap()
    private val requestsByApprover: MutableMap<Long, MutableMap<Long, ApprovalRequest>> = ConcurrentHashMap()

    fun save(request: ApprovalRequest): ApprovalRequest = request.also {
        if (request.steps.isEmpty()) {
            throw IllegalArgumentException("Approval request ${request.requestId} must contain at least one approver")
        }

        val previous = requestsById.put(request.requestId, request)

        previous?.steps
            ?.map { it.approverId }
            ?.toSet()
            ?.forEach { approverId ->
                val requestsForApprover = requestsByApprover[approverId] ?: return@forEach
                requestsForApprover.remove(request.requestId)
                if (requestsForApprover.isEmpty()) {
                    requestsByApprover.remove(approverId)
                }
            }

        request.steps
            .filter { it.status == ApprovalStatus.PENDING }
            .map { it.approverId }
            .toSet()
            .forEach { approverId ->
                val requestsForApprover = requestsByApprover.getOrPut(approverId) { mutableMapOf() }
                requestsForApprover[request.requestId] = request
            }
    }

    fun findById(id: Int): ApprovalRequest? = findByRequestId(id.toLong())

    fun findByRequestId(requestId: Long): ApprovalRequest? = requestsById[requestId]

    fun findAll(): List<ApprovalRequest> = requestsById.values.toList()

    fun findPendingByApproverId(approverId: Long): List<ApprovalRequest> =
        requestsByApprover[approverId]
            ?.values
            ?.filter { request ->
                request.steps.any { it.approverId == approverId && it.status == ApprovalStatus.PENDING }
            }
            ?.toList()
            ?: emptyList()

    fun deleteByRequestId(requestId: Long) {
        val removed = requestsById.remove(requestId) ?: return

        removed.steps
            .map { it.approverId }
            .toSet()
            .forEach { approverId ->
                val requestsForApprover = requestsByApprover[approverId] ?: return@forEach
                requestsForApprover.remove(requestId)
                if (requestsForApprover.isEmpty()) {
                    requestsByApprover.remove(approverId)
                }
            }
    }

    fun deleteById(id: Int) {
        deleteByRequestId(id.toLong())
    }

    fun clear() {
        requestsById.clear()
        requestsByApprover.clear()
    }
}
