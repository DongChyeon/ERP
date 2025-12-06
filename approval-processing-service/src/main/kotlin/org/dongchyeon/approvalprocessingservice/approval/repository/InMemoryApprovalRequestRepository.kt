package org.dongchyeon.approvalprocessingservice.approval.repository

import org.dongchyeon.approvalprocessingservice.approval.model.ApprovalRequest
import org.dongchyeon.approvalprocessingservice.approval.model.ApprovalStatus
import org.springframework.stereotype.Repository

@Repository
class InMemoryApprovalRequestRepository {
    private val storage: MutableMap<Long, ApprovalRequest> = mutableMapOf()

    fun save(request: ApprovalRequest): ApprovalRequest = request.also {
        val approverId = request.steps.firstOrNull()?.approverId
            ?: throw IllegalArgumentException("Approval request ${request.requestId} must contain at least one approver")
        storage[approverId] = request
    }

    fun findById(id: Int): ApprovalRequest? = findByRequestId(id.toLong())

    fun findByRequestId(requestId: Long): ApprovalRequest? =
        storage.values.firstOrNull { it.requestId == requestId }

    fun findAll(): List<ApprovalRequest> = storage.values.toList()

    fun findPendingByApproverId(approverId: Long): List<ApprovalRequest> =
        storage[approverId]
            ?.takeIf { request ->
                request.steps.any { it.approverId == approverId && it.status == ApprovalStatus.PENDING }
            }
            ?.let { listOf(it) }
            ?: emptyList()

    fun deleteById(id: Int) {
        val iterator = storage.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.value.requestId == id.toLong()) {
                iterator.remove()
                break
            }
        }
    }

    fun clear() {
        storage.clear()
    }
}
