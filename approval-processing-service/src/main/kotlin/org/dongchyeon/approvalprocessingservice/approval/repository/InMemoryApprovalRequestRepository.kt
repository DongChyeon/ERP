package org.dongchyeon.approvalprocessingservice.approval.repository

import org.dongchyeon.approvalprocessingservice.approval.model.ApprovalRequest
import org.dongchyeon.approvalprocessingservice.approval.model.ApprovalStatus
import org.springframework.stereotype.Repository

@Repository
class InMemoryApprovalRequestRepository {
    private val requestsById: MutableMap<Long, ApprovalRequest> = mutableMapOf()
    private val requestsByApprover: MutableMap<Long, MutableList<ApprovalRequest>> = mutableMapOf()

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
                requestsForApprover.removeIf { it.requestId == request.requestId }
                if (requestsForApprover.isEmpty()) {
                    requestsByApprover.remove(approverId)
                }
            }

        request.steps
            .map { it.approverId }
            .toSet()
            .forEach { approverId ->
                val requestsForApprover = requestsByApprover.getOrPut(approverId) { mutableListOf() }
                requestsForApprover.add(request)
            }
    }

    fun findById(id: Int): ApprovalRequest? = findByRequestId(id.toLong())

    fun findByRequestId(requestId: Long): ApprovalRequest? = requestsById[requestId]

    fun findAll(): List<ApprovalRequest> = requestsById.values.toList()

    fun findPendingByApproverId(approverId: Long): List<ApprovalRequest> =
        requestsByApprover[approverId]
            ?.filter { request ->
                request.steps.any { it.approverId == approverId && it.status == ApprovalStatus.PENDING }
            }
            ?.toList()
            ?: emptyList()

    fun deleteById(id: Int) {
        val requestId = id.toLong()
        val removed = requestsById.remove(requestId) ?: return

        removed.steps
            .map { it.approverId }
            .toSet()
            .forEach { approverId ->
                val requestsForApprover = requestsByApprover[approverId] ?: return@forEach
                requestsForApprover.removeIf { it.requestId == requestId }
                if (requestsForApprover.isEmpty()) {
                    requestsByApprover.remove(approverId)
                }
            }
    }

    fun clear() {
        requestsById.clear()
        requestsByApprover.clear()
    }
}
