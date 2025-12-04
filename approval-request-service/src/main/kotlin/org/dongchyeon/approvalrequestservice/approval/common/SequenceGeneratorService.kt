package org.dongchyeon.approvalrequestservice.approval.common

import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service

private const val APPROVAL_REQUEST_SEQUENCE = "approval_request_sequence"

@Service
class SequenceGeneratorService(
    private val mongoOperations: MongoOperations,
) {
    fun generateApprovalRequestId(): Long {
        val query = Query.query(Criteria.where("_id").`is`(APPROVAL_REQUEST_SEQUENCE))
        val update = Update().inc("seq", 1)
        val options = FindAndModifyOptions.options().returnNew(true).upsert(true)

        val sequence = mongoOperations.findAndModify(query, update, options, DatabaseSequence::class.java)
        return sequence?.seq ?: 1
    }
}
