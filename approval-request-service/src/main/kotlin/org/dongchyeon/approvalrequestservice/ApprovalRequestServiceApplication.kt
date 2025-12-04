package org.dongchyeon.approvalrequestservice

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class ApprovalRequestServiceApplication {

    private val log = LoggerFactory.getLogger(ApprovalRequestServiceApplication::class.java)

    @Bean
    fun mongoClient(@Value("\${spring.data.mongodb.uri}") mongoUri: String): MongoClient {
        log.info("Configuring MongoClient with URI={}", mongoUri)
        val settings = MongoClientSettings.builder()
            .applyConnectionString(ConnectionString(mongoUri))
            .build()
        return MongoClients.create(settings)
    }
}

fun main(args: Array<String>) {
	runApplication<ApprovalRequestServiceApplication>(*args)
}
