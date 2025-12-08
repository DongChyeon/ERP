package org.dongchyeon.approvalprocessingservice.approval.service

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.dongchyeon.approvalprocessingservice.approval.messaging.ApprovalResultPublisher
import org.dongchyeon.approvalprocessingservice.approval.model.ApprovalDecisionCommand
import org.dongchyeon.approvalprocessingservice.approval.model.ApprovalRequest
import org.dongchyeon.approvalprocessingservice.approval.model.ApprovalStatus
import org.dongchyeon.approvalprocessingservice.approval.model.ApprovalStep
import org.dongchyeon.approvalprocessingservice.approval.repository.InMemoryApprovalRequestRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class ApprovalDecisionServiceTest {

    private val repository = InMemoryApprovalRequestRepository()
    private val approvalResultPublisher = mockk<ApprovalResultPublisher>()
    private val service = ApprovalDecisionService(repository, approvalResultPublisher)

    @AfterEach
    fun tearDown() {
        repository.clear()
    }

    @Test
    fun `결재가_반려되면_요청을_삭제한다`() {
        every { approvalResultPublisher.publish(any()) } just Runs
        repository.save(defaultApprovalRequest())

        val command = ApprovalDecisionCommand(
            requestId = REQUEST_ID,
            approverId = PRIMARY_APPROVER_ID,
            status = ApprovalStatus.REJECTED,
        )

        service.decide(command)

        verify(exactly = 1) {
            approvalResultPublisher.publish(withArg { payload ->
                assertThat(payload.requestId).isEqualTo(REQUEST_ID)
                assertThat(payload.step).isEqualTo(1)
                assertThat(payload.approverId).isEqualTo(PRIMARY_APPROVER_ID)
                assertThat(payload.status).isEqualTo(ApprovalStatus.REJECTED)
            })
        }
        assertThat(repository.findByRequestId(REQUEST_ID)).isNull()
        assertThat(repository.findPendingByApproverId(PRIMARY_APPROVER_ID)).isEmpty()
        assertThat(repository.findPendingByApproverId(SECONDARY_APPROVER_ID)).isEmpty()
    }

    @Test
    fun `결재가_승인되어도_다음_결재자가_남아있으면_요청을_유지한다`() {
        every { approvalResultPublisher.publish(any()) } just Runs
        repository.save(defaultApprovalRequest())

        val command = ApprovalDecisionCommand(
            requestId = REQUEST_ID,
            approverId = PRIMARY_APPROVER_ID,
            status = ApprovalStatus.APPROVED,
        )

        service.decide(command)

        verify(exactly = 1) {
            approvalResultPublisher.publish(withArg { payload ->
                assertThat(payload.requestId).isEqualTo(REQUEST_ID)
                assertThat(payload.step).isEqualTo(1)
                assertThat(payload.approverId).isEqualTo(PRIMARY_APPROVER_ID)
                assertThat(payload.status).isEqualTo(ApprovalStatus.APPROVED)
            })
        }
        val saved = repository.findByRequestId(REQUEST_ID)
        assertThat(saved).isNotNull()
        assertThat(saved!!.steps.first { it.approverId == PRIMARY_APPROVER_ID }.status)
            .isEqualTo(ApprovalStatus.APPROVED)
        assertThat(saved.steps.first { it.approverId == SECONDARY_APPROVER_ID }.status)
            .isEqualTo(ApprovalStatus.PENDING)
    }

    private fun defaultApprovalRequest(): ApprovalRequest =
        ApprovalRequest(
            requestId = REQUEST_ID,
            requesterId = 99L,
            title = "Expense",
            content = "Please approve",
            steps = listOf(
                ApprovalStep(
                    step = 1,
                    approverId = PRIMARY_APPROVER_ID,
                    status = ApprovalStatus.PENDING,
                ),
                ApprovalStep(
                    step = 2,
                    approverId = SECONDARY_APPROVER_ID,
                    status = ApprovalStatus.PENDING,
                ),
            ),
        )

    private companion object {
        private const val REQUEST_ID = 1L
        private const val PRIMARY_APPROVER_ID = 42L
        private const val SECONDARY_APPROVER_ID = 43L
    }
}
