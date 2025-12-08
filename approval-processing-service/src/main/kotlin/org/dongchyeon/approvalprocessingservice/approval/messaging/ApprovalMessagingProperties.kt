package org.dongchyeon.approvalprocessingservice.approval.messaging

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "approval.messaging")
data class ApprovalMessagingProperties(
    val requestExchange: String = "approval.requests.exchange",
    val requestRoutingKey: String = "approval.requests",
    val requestQueue: String = "approval.requests.queue",
    val resultExchange: String = "approval.results.exchange",
    val resultRoutingKey: String = "approval.results",
    val resultQueue: String = "approval.results.queue",
)
