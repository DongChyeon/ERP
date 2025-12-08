package org.dongchyeon.common.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import org.dongchyeon.common.messaging.ApprovalStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class GsonConfig {

    @Bean
    open fun gson(): Gson =
        GsonBuilder()
            .registerTypeAdapter(
                ApprovalStatus::class.java,
                object : TypeAdapter<ApprovalStatus>() {
                    override fun write(out: JsonWriter, value: ApprovalStatus?) {
                        if (value == null) {
                            out.nullValue()
                        } else {
                            out.value(value.value)
                        }
                    }

                    override fun read(`in`: JsonReader): ApprovalStatus {
                        if (`in`.peek() == JsonToken.NULL) {
                            `in`.nextNull()
                            return ApprovalStatus.PENDING
                        }
                        return ApprovalStatus.from(`in`.nextString())
                    }
                },
            )
            .create()
}
