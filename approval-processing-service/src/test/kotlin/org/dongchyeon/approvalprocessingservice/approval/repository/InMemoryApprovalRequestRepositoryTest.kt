package org.dongchyeon.approvalprocessingservice.approval.repository

import org.assertj.core.api.Assertions.assertThat
import org.dongchyeon.approvalprocessingservice.approval.model.ApprovalRequest
import org.dongchyeon.approvalprocessingservice.approval.model.ApprovalStatus
import org.dongchyeon.approvalprocessingservice.approval.model.ApprovalStep
import org.junit.jupiter.api.Test

class InMemoryApprovalRequestRepositoryTest {

    private val repository = InMemoryApprovalRequestRepository()

    @Test
    fun `승인한_요청은_인메모리_DB에서_제거한다`() {
        val approverId = 42L
        val request = ApprovalRequest(
            requestId = 1L,
            requesterId = 7L,
            title = "Budget",
            content = "Approve budget",
            steps = listOf(
                ApprovalStep(
                    step = 1,
                    approverId = approverId,
                    status = ApprovalStatus.PENDING,
                ),
            ),
        )

        repository.save(request)
        assertThat(repository.findPendingByApproverId(approverId)).hasSize(1)

        val decidedRequest = request.copy(
            steps = listOf(
                request.steps.first().copy(status = ApprovalStatus.APPROVED),
            ),
        )

        repository.save(decidedRequest)
        assertThat(repository.findPendingByApproverId(approverId)).isEmpty()
    }

    @Test
    fun `반려한_요청은_인메모리_DB에서_제거한다`() {
        val approverId = 42L
        val request = ApprovalRequest(
            requestId = 1L,
            requesterId = 7L,
            title = "Budget",
            content = "Approve budget",
            steps = listOf(
                ApprovalStep(
                    step = 1,
                    approverId = approverId,
                    status = ApprovalStatus.PENDING,
                ),
            ),
        )

        repository.save(request)
        assertThat(repository.findPendingByApproverId(approverId)).hasSize(1)

        val decidedRequest = request.copy(
            steps = listOf(
                request.steps.first().copy(status = ApprovalStatus.REJECTED),
            ),
        )

        repository.save(decidedRequest)
        assertThat(repository.findPendingByApproverId(approverId)).isEmpty()
    }
}
