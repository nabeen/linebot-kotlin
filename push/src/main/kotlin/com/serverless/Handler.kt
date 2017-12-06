package com.serverless

import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.linecorp.bot.client.LineMessagingService
import com.linecorp.bot.client.LineMessagingServiceBuilder
import com.linecorp.bot.model.ReplyMessage
import com.linecorp.bot.model.PushMessage
import com.linecorp.bot.model.event.*
import com.linecorp.bot.model.event.message.*
import com.linecorp.bot.model.message.Message
import com.linecorp.bot.model.message.TextMessage
import com.linecorp.bot.model.response.BotApiResponse
import jdk.internal.util.xml.impl.Input
import org.apache.log4j.Logger
import retrofit2.Response

import java.io.IOException
import java.util.ArrayList

class Handler : RequestHandler<Input, ApiGatewayResponse> {

    /***
     * Messaging APIリクエストを受けて、Reply messageの送信などを行います。
     * @param callbackRequest Messaging APIリクエスト内容
     */
    private fun push() {
        val textMessage: TextMessage = TextMessage("hello");
        val pushMessage = PushMessage(USER_ID, textMessage);

        val client = LineMessagingServiceBuilder.create(CHANNEL_ACCESS_TOKEN).build()

        try {
            val response = client.pushMessage(pushMessage).execute()
            if (response.isSuccessful) {
                LOG.info(response.message())
            } else {
                LOG.warn(response.errorBody().string())
            }
        } catch (e1: IOException) {
            LOG.error(e1)
        }
    }

    override fun handleRequest(input: Input, context: Context): ApiGatewayResponse {

        try {
            push()
        } catch (e: Exception) {
            LOG.error(e)
        }

        return ApiGatewayResponse.builder().setStatusCode(200).build()
    }

    @Throws(IOException::class)
    private fun buildCallbackRequest(): CallbackRequest {
        val message = "true".toString()

        return buildObjectMapper().readValue<CallbackRequest>(message, CallbackRequest::class.java)
    }

    companion object {

        private val LOG = Logger.getLogger(Handler::class.java)

        private val CHANNEL_SECRET = System.getenv("CHANNEL_SECRET")
        private val CHANNEL_ACCESS_TOKEN = System.getenv("CHANNEL_ACCESS_TOKEN")
        private val USER_ID = System.getenv("USER_ID")

        private fun buildObjectMapper(): ObjectMapper {
            val objectMapper = ObjectMapper()
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

            // Register JSR-310(java.time.temporal.*) module and read number as
            // millsec.
            objectMapper.registerModule(JavaTimeModule())
                    .configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
            return objectMapper
        }
    }

}
