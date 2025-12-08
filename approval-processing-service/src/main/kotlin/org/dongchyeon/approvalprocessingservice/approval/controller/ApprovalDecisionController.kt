package org.dongchyeon.approvalprocessingservice.approval.controller

import jakarta.validation.Valid
import org.dongchyeon.approvalprocessingservice.approval.model.ApprovalDecisionCommand
import org.dongchyeon.common.messaging.ApprovalStatus
import org.dongchyeon.approvalprocessingservice.approval.model.ProcessApprovalResultRequest
import org.dongchyeon.approvalprocessingservice.approval.model.ProcessApprovalResultResponse
import org.dongchyeon.approvalprocessingservice.approval.service.ApprovalDecisionService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/process")
class ApprovalDecisionController(
    private val approvalDecisionService: ApprovalDecisionService,
) {

    @PostMapping("/{approverId}/{requestId}")
    fun processApprovalResult(
        @PathVariable approverId: Long,
        @PathVariable requestId: Long,
        @Valid @RequestBody request: ProcessApprovalResultRequest,
    ): ProcessApprovalResultResponse {
        val status = try {
            approvalDecisionService.parseStatus(request.status)
        } catch (ex: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, ex.message, ex)
        }

        approvalDecisionService.decide(
            ApprovalDecisionCommand(
                requestId = requestId,
                approverId = approverId,
                status = status,
            ),
        )
        return ProcessApprovalResultResponse(status = status.value)
    }
}
