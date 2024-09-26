package org.iamramtin.api.handlers

import com.fasterxml.jackson.databind.ObjectMapper
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.ext.web.RoutingContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.iamramtin.core.OrderBook
import org.iamramtin.models.Order
import org.iamramtin.models.api.toLimitOrderRequest

class OrderHandler(
    private val orderBook: OrderBook,
    private val mapper: ObjectMapper,
    private val coroutineScope: CoroutineScope
) {
    fun getOrderBook(ctx: RoutingContext) {
        coroutineScope.launch {
            try {
                val orderBookData = orderBook.getOrderBook()
                ctx.response()
                    .putHeader("content-type", "application/json")
                    .end(mapper.writeValueAsString(orderBookData))
            } catch (e: Exception) {
                ctx.response()
                    .setStatusCode(500)
                    .end("Error retrieving order book: ${e.message}")
            }
        }
    }

    fun submitLimitOrder(ctx: RoutingContext) {
        coroutineScope.launch {
            try {
                val request = ctx.body().asJsonObject().toLimitOrderRequest()
                val order = Order(side = request.side, price = request.price, quantity = request.quantity)

                val orderId = orderBook.submitLimitOrder(order)
                ctx.response()
                    .putHeader("content-type", "application/json")
                    .end(mapper.writeValueAsString(mapOf("id" to orderId)))
            } catch (e: Exception) {
                ctx.response()
                    .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                    .end("Error submitting limit order: ${e.message}")
            }
        }
    }
}