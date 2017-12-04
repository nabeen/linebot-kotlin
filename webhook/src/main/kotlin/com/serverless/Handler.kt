package com.serverless

import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.PutItemRequest
import com.amazonaws.services.dynamodbv2.model.PutItemResult
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.linecorp.bot.client.LineSignatureValidator
import org.apache.log4j.Logger

import java.nio.charset.StandardCharsets
import java.util.HashMap

class Handler : RequestHandler<Request, ApiGatewayResponse> {

    override fun handleRequest(input: Request, context: Context): ApiGatewayResponse {

        // Signature validation
        // https://devdocs.line.me/ja/#webhooks
        // リクエストの送信元がLINEであることを確認するために署名検証を行わなくてはなりません。
        try {
            if (!validate(CHANNEL_SECRET, input.decodedBody, input.signature!!)) {
                return ApiGatewayResponse.builder().setStatusCode(401).build()
            }
        } catch (e: IllegalArgumentException) {
            LOG.warn("Base64 decode fail.")
            return ApiGatewayResponse.builder().setStatusCode(401).build()
        }

        // DynamoDB登録
        try {
            putMessage(input.signature!!, input.decodedBody)
        } catch (e: Exception) {
            LOG.error("DynamoDB putItem fail.")
            return ApiGatewayResponse.builder().setStatusCode(401).build()
        }

        return ApiGatewayResponse.builder().setStatusCode(200).build()
    }

    /***
     * 署名検証を行います。
     * @param channelSecret
     * @param body
     * @param signature
     * @return
     */
    private fun validate(channelSecret: String, body: String, signature: String): Boolean {
        val lineSignatureValidator = LineSignatureValidator(channelSecret.toByteArray())

        if (lineSignatureValidator.validateSignature(body.toByteArray(StandardCharsets.UTF_8), signature)) {
            LOG.info("Validate OK")
            return true
        } else {
            LOG.warn("Validate NG")
            return false
        }
    }

    /**
     * DynamoDBへの登録を行います。
     *
     * @param signature
     * @param body
     * @return
     * @throws Exception
     */
    @Throws(Exception::class)
    private fun putMessage(signature: String, body: String): Boolean {
        val dynamoDb = AmazonDynamoDBClient.builder()
                .withRegion(Regions.AP_NORTHEAST_1)
                .build()

        val item = HashMap<String, AttributeValue>()
        item.put("id", AttributeValue(signature))
        item.put("message", AttributeValue(body))
        val itemRequest = PutItemRequest("LineBotDynamodb", item)
        val result = dynamoDb.putItem(itemRequest)
        LOG.debug(result.toString())

        return true
    }

    companion object {

        private val LOG = Logger.getLogger(Handler::class.java)

        private val CHANNEL_SECRET = System.getenv("CHANNEL_SECRET")
    }
}
