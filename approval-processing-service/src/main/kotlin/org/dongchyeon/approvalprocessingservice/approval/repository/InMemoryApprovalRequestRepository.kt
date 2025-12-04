package org.dongchyeon.approvalprocessingservice.approval.repository

import org.dongchyeon.approvalprocessingservice.approval.model.ApprovalRequest
import org.dongchyeon.approvalprocessingservice.approval.model.ApprovalStatus
import org.springframework.stereotype.Repository

@Repository
class InMemoryApprovalRequestRepository {
    private val storage: HashMap<Int, ApprovalRequest> = HashMap()

    fun save(request: ApprovalRequest): ApprovalRequest = request.also {
        storage[request.requestId.toInt()] = request
    }

    fun findById(id: Int): ApprovalRequest? = storage[id]

    fun findByRequestId(requestId: Long): ApprovalRequest? = storage[requestId.toInt()]

    fun findAll(): List<ApprovalRequest> = storage.values.toList()

    fun findPendingByApproverId(approverId: Long): List<ApprovalRequest> =
        storage.values.filter { request ->
            request.steps.any { it.approverId == approverId && it.status == ApprovalStatus.PENDING }
        }

    fun deleteById(id: Int) {
        storage.remove(id)
    }

    fun clear() {
        storage.clear()
    }
}
