package org.dongchyeon.approvalrequestservice.approval

import jakarta.validation.Valid
import org.dongchyeon.approvalrequestservice.approval.model.CreateApprovalRequest
import org.dongchyeon.approvalrequestservice.approval.model.CreateApprovalResponse
import org.dongchyeon.approvalrequestservice.approval.service.ApprovalRequestService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/approvals")
class ApprovalRequestController(
    private val approvalRequestService: ApprovalRequestService,
) {
    @PostMapping
    fun createApprovalRequest(
        @Valid @RequestBody request: CreateApprovalRequest,
    ): CreateApprovalResponse = approvalRequestService.create(request)
}
