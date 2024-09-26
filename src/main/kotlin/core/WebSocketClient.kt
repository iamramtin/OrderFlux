package org.iamramtin.core

import io.ktor.websocket.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.iamramtin.models.OrderBookData
import org.iamramtin.models.OrderBookUpdateResponse

/**
 * Client for interacting with the VALR WebSocket API.
 * It's responsible for fetching the initial order book snapshot.
 */
class WebSocketClient(private val httpClient: HttpClient = HttpClient(CIO) {
    install(WebSockets)
}) {
    private val json = Json { ignoreUnknownKeys = true }
    private val socketUrl = "wss://api.valr.com/ws/trade"
    private val orderPairs = listOf("BTCZAR")
    private val message = """
    {
        "type": "SUBSCRIBE",
        "subscriptions": [
            {
                "event": "FULL_ORDERBOOK_UPDATE",
                "pairs": $orderPairs
            }
        ]
    }
    """.trimIndent()

    /**
     * Fetches the order book snapshot from the WebSocket API.
     * @return OrderBookData containing the snapshot
     * @throws Exception if unable to receive the snapshot
     */
    suspend fun getOrderBookSnapshot(): OrderBookData {
        var orderBookData: OrderBookData? = null

        try {
            httpClient.webSocket(urlString = socketUrl) {
                send(Frame.Text(message))

                for (frame in incoming) {
                    when (frame) {
                        is Frame.Text -> {
                            val receivedMessage = frame.readText()
                            orderBookData = processMessage(receivedMessage)
                            if (orderBookData != null) {
                                break // Exit the loop once we've received the snapshot
                            }
                        }
                        else -> println("Received unexpected frame type: $frame")
                    }
                }
            }
        } catch (e: Exception) {
            println("Error in WebSocket connection: $e")
        }

        return orderBookData ?: throw Exception("Failed to receive order book snapshot")
    }

    /**
     * Processes incoming WebSocket messages.
     * @param message The received message as a string
     * @return OrderBookData if the message is the initial orderbook snapshot, null otherwise
     */
    private fun processMessage(message: String): OrderBookData? {
        val data = json.parseToJsonElement(message).jsonObject

        return when (val type = data["type"]?.jsonPrimitive?.content) {
            "SUBSCRIBED" -> {
                null
            }
            "FULL_ORDERBOOK_SNAPSHOT" -> {
                val response = json.decodeFromString<OrderBookUpdateResponse>(message)
                println("Received order book snapshot")
                response.data
            }
            else -> {
                println("Received unknown message type: $type")
                null
            }
        }
    }
}