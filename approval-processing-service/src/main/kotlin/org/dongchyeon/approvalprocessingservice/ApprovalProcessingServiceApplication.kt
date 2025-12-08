package org.dongchyeon.approvalprocessingservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import
import org.dongchyeon.common.config.GsonConfig

@SpringBootApplication
@Import(GsonConfig::class)
class ApprovalProcessingServiceApplication

fun main(args: Array<String>) {
    runApplication<ApprovalProcessingServiceApplication>(*args)
}
