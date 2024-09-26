package api

import io.vertx.core.Vertx
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import io.vertx.ext.web.client.WebClient
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.coAwait
import org.iamramtin.api.Server

@ExtendWith(VertxExtension::class)
class ServerTest {

    private lateinit var vertx: Vertx
    private lateinit var client: WebClient
    private lateinit var authToken: String

    @BeforeEach
    fun setUp(vertx: Vertx, testContext: VertxTestContext) {
        this.vertx = vertx
        this.client = WebClient.create(vertx)

        runBlocking {
            val server = Server()
            vertx.deployVerticle(server).coAwait()

            // Login to get auth token
            val loginResponse = client.post(8888, "localhost", "/login")
                .sendJsonObject(JsonObject()
                    .put("username", "username")
                    .put("password", "password")
                ).coAwait()

            assertEquals(200, loginResponse.statusCode())
            authToken = loginResponse.bodyAsJsonObject().getString("token")
            assertNotNull(authToken)

            testContext.completeNow()
        }
    }


    @Test
    fun testGetOrderBook(testContext: VertxTestContext) = runBlocking {
        val response = client.get(8888, "localhost", "/BTCZAR/orderbook")
            .putHeader("Authorization", "Bearer $authToken")
            .send().coAwait()

        assertEquals(200, response.statusCode())
        val json = response.bodyAsJsonObject()
        assertTrue(json.containsKey("bids"))
        assertTrue(json.containsKey("asks"))
        testContext.completeNow()
    }

    @Test
    fun testSubmitLimitOrder(testContext: VertxTestContext) = runBlocking {
        val orderJson = JsonObject()
            .put("side", "BUY")
            .put("price", 50000.0)
            .put("quantity", 1.0)

        val response = client.post(8888, "localhost", "/orders/limit")
            .putHeader("Authorization", "Bearer $authToken")
            .sendJsonObject(orderJson).coAwait()

        assertEquals(200, response.statusCode())
        val json = response.bodyAsJsonObject()
        assertTrue(json.containsKey("id"))
        testContext.completeNow()
    }

    @Test
    fun testGetTradeHistory(testContext: VertxTestContext) = runBlocking {
        val response = client.get(8888, "localhost", "/BTCZAR/tradehistory")
            .putHeader("Authorization", "Bearer $authToken")
            .send().coAwait()

        assertEquals(200, response.statusCode())
        val json = response.bodyAsJsonArray()
        assertNotNull(json)
        testContext.completeNow()
    }

    @Test
    fun testGetNonExistentTrade(testContext: VertxTestContext) = runBlocking {
        val server = Server()
        vertx.deployVerticle(server).coAwait()

        val response = client.get(8888, "localhost", "/BTCZAR/trade/nonexistent")
            .putHeader("Authorization", "Bearer $authToken")
            .send().coAwait()

        assertEquals(404, response.statusCode())
        assertEquals("Trade not found", response.bodyAsString())

        testContext.completeNow()
    }

    @Test
    fun testOrderBookInitialization(testContext: VertxTestContext) = runBlocking {
        val response = client.post(8888, "localhost", "/orderbook/init")
            .putHeader("Authorization", "Bearer $authToken")
            .send().coAwait()

        assertEquals(200, response.statusCode())
        val json = response.bodyAsJsonObject()
        assertEquals("Order book repopulated with sample data", json.getString("message"))
        testContext.completeNow()
    }

    @Test
    fun testInvalidEndpoint(testContext: VertxTestContext) = runBlocking {
        val response = client.get(8888, "localhost", "/invalid/endpoint")
            .putHeader("Authorization", "Bearer $authToken")
            .send().coAwait()

        assertEquals(404, response.statusCode())
        testContext.completeNow()
    }

    @Test
    fun testInvalidOrderSubmission(testContext: VertxTestContext) = runBlocking {
        val invalidOrderJson = JsonObject()
            .put("side", "INVALID")
            .put("price", -50000.0)
            .put("quantity", 0.0)

        val response = client.post(8888, "localhost", "/orders/limit")
            .putHeader("Authorization", "Bearer $authToken")
            .sendJsonObject(invalidOrderJson).coAwait()

        assertEquals(400, response.statusCode())
        testContext.completeNow()
    }

    @Test
    fun testUnauthorizedAccess(testContext: VertxTestContext) = runBlocking {
        val response = client.get(8888, "localhost", "/BTCZAR/orderbook")
            .send().coAwait()

        assertEquals(401, response.statusCode())
        testContext.completeNow()
    }
}