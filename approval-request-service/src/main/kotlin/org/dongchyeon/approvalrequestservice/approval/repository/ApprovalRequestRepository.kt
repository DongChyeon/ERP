package org.dongchyeon.approvalrequestservice.approval.repository

import org.springframework.data.mongodb.repository.MongoRepository
import org.dongchyeon.approvalrequestservice.approval.model.ApprovalRequestDocument

interface ApprovalRequestRepository : MongoRepository<ApprovalRequestDocument, Long>
