package org.dongchyeon.approvalprocessingservice.approval.controller

import org.dongchyeon.approvalprocessingservice.approval.model.ApprovalRequest
import org.dongchyeon.approvalprocessingservice.approval.repository.InMemoryApprovalRequestRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/process")
class ApprovalProcessController(
    private val approvalRequestRepository: InMemoryApprovalRequestRepository,
) {

    @GetMapping("/{approverId}")
    fun getPendingRequests(@PathVariable approverId: Long): List<ApprovalRequest> =
        approvalRequestRepository.findPendingByApproverId(approverId)
}
