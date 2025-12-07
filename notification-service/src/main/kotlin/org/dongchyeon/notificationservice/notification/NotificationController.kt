package org.dongchyeon.notificationservice.notification

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/notifications")
class NotificationController(
    private val notificationSender: NotificationSender,
) {

    @PostMapping("/final-status")
    fun publishFinalStatus(@RequestBody request: FinalStatusNotificationRequest): ResponseEntity<Void> {
        notificationSender.sendFinalStatus(request)
        return ResponseEntity.accepted().build()
    }
}
