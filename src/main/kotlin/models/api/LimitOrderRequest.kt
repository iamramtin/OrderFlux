package org.iamramtin.models.api

import io.vertx.core.json.JsonObject
import org.iamramtin.models.OrderSide

data class LimitOrderRequest(
    val side: OrderSide,
    val price: Double,
    val quantity: Double
)

fun JsonObject.toLimitOrderRequest(): LimitOrderRequest {
    val side = try {
        OrderSide.valueOf(this.getString("side")?.uppercase()
            ?: throw IllegalArgumentException("Missing 'side'"))
    } catch (e: IllegalArgumentException) {
        throw IllegalArgumentException("Invalid 'side'. Must be BUY or SELL.")
    }

    val price = this.getDouble("price")
        ?: throw IllegalArgumentException("Missing 'price'")
    if (price <= 0) throw IllegalArgumentException("'price' must be greater than 0")

    val quantity = this.getDouble("quantity")
        ?: throw IllegalArgumentException("Missing 'quantity'")
    if (quantity <= 0) throw IllegalArgumentException("'quantity' must be greater than 0")

    return LimitOrderRequest(side, price, quantity)
}
