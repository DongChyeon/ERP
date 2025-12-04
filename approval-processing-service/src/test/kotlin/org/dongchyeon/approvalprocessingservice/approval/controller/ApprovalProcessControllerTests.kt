package org.dongchyeon.approvalprocessingservice.approval.controller

import org.dongchyeon.approvalprocessingservice.approval.model.ApprovalRequest
import org.dongchyeon.approvalprocessingservice.approval.model.ApprovalStatus
import org.dongchyeon.approvalprocessingservice.approval.model.ApprovalStep
import org.dongchyeon.approvalprocessingservice.approval.repository.InMemoryApprovalRequestRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import kotlin.test.assertEquals

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApprovalProcessControllerTests @Autowired constructor(
    private val applicationContext: ApplicationContext,
    private val repository: InMemoryApprovalRequestRepository,
) {

    private lateinit var webTestClient: WebTestClient

    @BeforeEach
    fun setUp() {
        repository.clear()
        webTestClient = WebTestClient.bindToApplicationContext(applicationContext)
            .configureClient()
            .build()
    }

    @Test
    fun `지정된 결재자의 대기 목록을 반환한다`() {
        repository.save(
            ApprovalRequest(
                requestId = 1,
                requesterId = 10,
                title = "Payment",
                content = "Approve payment",
                steps = listOf(
                    ApprovalStep(step = 1, approverId = 111, status = ApprovalStatus.PENDING),
                    ApprovalStep(step = 2, approverId = 222, status = ApprovalStatus.PENDING),
                ),
            ),
        )

        repository.save(
            ApprovalRequest(
                requestId = 2,
                requesterId = 20,
                title = "Travel",
                content = "Approve travel",
                steps = listOf(
                    ApprovalStep(step = 1, approverId = 333, status = ApprovalStatus.PENDING),
                ),
            ),
        )

        val response = webTestClient.get()
            .uri("/process/{approverId}", 111)
            .exchange()
            .expectStatus().isOk
            .expectBody<List<ApprovalRequest>>()
            .returnResult()
            .responseBody ?: emptyList()

        assertEquals(1, response.size)
        assertEquals(1, response.first().requestId)
    }

    @Test
    fun `지정된 결재자의 대기 중인 요청이 없으면 빈 목록을 반환한다`() {
        repository.save(
            ApprovalRequest(
                requestId = 3,
                requesterId = 30,
                title = "Contract",
                content = "Approve contract",
                steps = listOf(
                    ApprovalStep(step = 1, approverId = 111, status = ApprovalStatus.APPROVED),
                ),
            ),
        )

        val response = webTestClient.get()
            .uri("/process/{approverId}", 444)
            .exchange()
            .expectStatus().isOk
            .expectBody<List<ApprovalRequest>>()
            .returnResult()
            .responseBody ?: emptyList()

        assertEquals(0, response.size)
    }
}
