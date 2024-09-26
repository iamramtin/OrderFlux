package org.iamramtin.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.kotlin.coroutines.CoroutineRouterSupport
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import org.iamramtin.api.handlers.OrderHandler
import org.iamramtin.api.handlers.TradeHandler
import org.iamramtin.core.OrderBook
import org.iamramtin.core.WebSocketClient

/**
 * Main server class that sets up and runs the HTTP server.
 * It handles routing, authentication, and initialises the order book.
 */
class Server : CoroutineVerticle(), CoroutineRouterSupport {
    private lateinit var authentication: Authentication
    private lateinit var webSocketClient: WebSocketClient
    private lateinit var orderBook: OrderBook
    private lateinit var mapper: ObjectMapper
    private lateinit var orderHandler: OrderHandler
    private lateinit var tradeHandler: TradeHandler

    /**
     * Main server class that sets up and runs the HTTP server.
     * It handles routing, authentication, and initialises the order book.
     */
    override suspend fun start() {
        val coroutineScope = CoroutineScope(vertx.dispatcher())

        // Initialise components
        authentication = Authentication(vertx)
        webSocketClient = WebSocketClient()
        orderBook = OrderBook()

        // Configure JSON mapper for serialization and deserialization
        mapper = jacksonObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

        // Initialise handlers for order and trade requests
        orderHandler = OrderHandler(orderBook, mapper, coroutineScope)
        tradeHandler = TradeHandler(orderBook, mapper, coroutineScope)

        // Set up router
        val router = Router.router(vertx)
        router.route().handler(BodyHandler.create())

        // Setup authentication and routes
        setup(router)

        // Initialise order book with snapshot data
        initialiseWithSnapshotData()

        // Start HTTP server
        vertx.createHttpServer()
            .requestHandler(router)
            .listen(8888) { result ->
                if (result.succeeded()) {
                    println("HTTP server started on port 8888")
                } else {
                    println("Failed to start HTTP server: ${result.cause()}")
                }
            }
    }

    /**
     * Sets up authentication and routes.
     * @param router The router to set up
     */
    private fun setup(router: Router) {
        authentication.setup(router)

        // Route to reinitialise the order book
        router.post("/orderbook/init").handler { ctx ->
            launch(vertx.dispatcher()) {
                initialiseWithSnapshotData()
                ctx.response()
                    .putHeader("content-type", "application/json")
                    .end(mapper.writeValueAsString(mapOf("message" to "Order book repopulated with sample data")))
            }
        }

        // Set up Routes for order and trade requests
        router.get("/BTCZAR/orderbook").handler(orderHandler::getOrderBook)
        router.post("/orders/limit").handler(orderHandler::submitLimitOrder)
        router.get("/BTCZAR/tradehistory").handler(tradeHandler::getRecentTrades)
        router.get("/BTCZAR/trade/:id").handler { ctx ->
            val id = ctx.pathParam("id")
            tradeHandler.getTrade(ctx, id)
        }
    }

    /**
     * Initializes the order book with snapshot data from the WebSocket.
     */
    private suspend fun initialiseWithSnapshotData() {
        try {
            val data = webSocketClient.getOrderBookSnapshot()
            orderBook.initialise(data)
            println("Order book updated with latest snapshot data")
        } catch (e: Exception) {
            println("Failed to update order book with snapshot: ${e.message}")
            throw e
        }
    }
}