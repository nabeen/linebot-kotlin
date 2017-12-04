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
import com.linecorp.bot.model.event.*
import com.linecorp.bot.model.event.message.*
import com.linecorp.bot.model.message.Message
import com.linecorp.bot.model.message.TextMessage
import com.linecorp.bot.model.response.BotApiResponse
import org.apache.log4j.Logger
import retrofit2.Response

import java.io.IOException
import java.util.ArrayList

class Handler : RequestHandler<DynamodbEvent, ApiGatewayResponse> {

    /***
     * Messaging APIリクエストを受けて、Reply messageの送信などを行います。
     * @param callbackRequest Messaging APIリクエスト内容
     */
    private fun reply(callbackRequest: CallbackRequest) {
        callbackRequest.events.forEach { e ->

            if (e is MessageEvent<*>) {
                val messageEvent = e as MessageEvent<MessageContent>
                val replyToken = messageEvent.replyToken
                val content = messageEvent.message

                if (content is TextMessageContent) {
                    val message = content.text

                    val client = LineMessagingServiceBuilder.create(CHANNEL_ACCESS_TOKEN).build()

                    val replyMessages = ArrayList<Message>()
                    replyMessages.add(TextMessage(message))

                    try {
                        val response = client.replyMessage(ReplyMessage(replyToken, replyMessages)).execute()
                        if (response.isSuccessful) {
                            LOG.info(response.message())
                        } else {
                            LOG.warn(response.errorBody().string())
                        }
                    } catch (e1: IOException) {
                        LOG.error(e1)
                    }

                }
                if (content is ImageMessageContent) {
                }
                if (content is LocationMessageContent) {
                }
                if (content is AudioMessageContent) {
                }
                if (content is VideoMessageContent) {
                }
                if (content is StickerMessageContent) {
                }
                if (content is FileMessageContent) {
                } else {
                }
            } else if (e is UnfollowEvent) {
            } else if (e is FollowEvent) {
            } else if (e is JoinEvent) {
            } else if (e is LeaveEvent) {
            } else if (e is PostbackEvent) {
            } else if (e is BeaconEvent) {
            } else {
            }
        }
    }

    override fun handleRequest(ddbEvent: DynamodbEvent, context: Context): ApiGatewayResponse {

        ddbEvent.records.forEach { event ->
            try {
                val callbackRequest = buildCallbackRequest(event)
                reply(callbackRequest)
            } catch (e: Exception) {
                LOG.error(e)
            }
        }
        return ApiGatewayResponse.builder().setStatusCode(200).build()
    }

    @Throws(IOException::class)
    private fun buildCallbackRequest(record: DynamodbEvent.DynamodbStreamRecord): CallbackRequest {
        val image = record.dynamodb.newImage

        val id = image["id"]?.getS()
        val message = image["message"]?.getS()

        return buildObjectMapper().readValue<CallbackRequest>(message, CallbackRequest::class.java)
    }

    companion object {

        private val LOG = Logger.getLogger(Handler::class.java)

        private val CHANNEL_SECRET = System.getenv("CHANNEL_SECRET")
        private val CHANNEL_ACCESS_TOKEN = System.getenv("CHANNEL_ACCESS_TOKEN")

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
