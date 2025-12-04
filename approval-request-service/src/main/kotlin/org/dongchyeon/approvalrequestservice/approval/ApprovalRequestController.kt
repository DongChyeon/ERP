package org.dongchyeon.approvalrequestservice.approval

import jakarta.validation.Valid
import org.dongchyeon.approvalrequestservice.approval.model.ApprovalRequestResponse
import org.dongchyeon.approvalrequestservice.approval.model.CreateApprovalRequest
import org.dongchyeon.approvalrequestservice.approval.model.CreateApprovalResponse
import org.dongchyeon.approvalrequestservice.approval.service.ApprovalRequestService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/approvals")
class ApprovalRequestController(
    private val approvalRequestService: ApprovalRequestService,
) {
    @GetMapping
    fun getApprovalRequests(): List<ApprovalRequestResponse> =
        approvalRequestService.getApprovalRequests()

    @GetMapping("/{requestId}")
    fun getApprovalRequest(
        @PathVariable requestId: Long,
    ): ApprovalRequestResponse = approvalRequestService.getApprovalRequest(requestId)

    @PostMapping
    fun createApprovalRequest(
        @Valid @RequestBody request: CreateApprovalRequest,
    ): CreateApprovalResponse = approvalRequestService.create(request)
}
