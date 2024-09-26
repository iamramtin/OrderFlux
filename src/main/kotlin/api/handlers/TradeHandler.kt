package org.iamramtin.api.handlers

import com.fasterxml.jackson.databind.ObjectMapper
import io.vertx.ext.web.RoutingContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.iamramtin.core.OrderBook

class TradeHandler(
    private val orderBook: OrderBook,
    private val mapper: ObjectMapper,
    private val coroutineScope: CoroutineScope
) {
    fun getRecentTrades(ctx: RoutingContext) {
        coroutineScope.launch {
            try {
                val recentTrades = orderBook.getRecentTrades()
                ctx.response()
                    .putHeader("content-type", "application/json")
                    .end(mapper.writeValueAsString(recentTrades))
            } catch (e: Exception) {
                ctx.response()
                    .setStatusCode(500)
                    .end("Error retrieving recent trades: ${e.message}")
            }
        }
    }

    fun getTrade(ctx: RoutingContext, id: String) {
        coroutineScope.launch {
            try {
                val trade = orderBook.getTrade(id)
                if (trade != null) {
                    ctx.response()
                        .putHeader("content-type", "application/json")
                        .end(mapper.writeValueAsString(trade))
                } else {
                    ctx.response()
                        .setStatusCode(404)
                        .end("Trade not found")
                }
            } catch (e: Exception) {
                ctx.response()
                    .setStatusCode(500)
                    .end("Error retrieving trade: ${e.message}")
            }
        }
    }
}