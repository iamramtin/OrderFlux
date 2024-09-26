package org.iamramtin.models

import java.time.Instant
import java.util.*

enum class OrderSide {
    BUY, SELL
}

data class Order(
    val id: String = UUID.randomUUID().toString(),
    val side: OrderSide,
    val price: Double,
    var quantity: Double,
    val timestamp: Instant = Instant.now()
)

data class Trade(
    val id: String,
    val price: Double,
    val quantity: Double,
    val takerSide: OrderSide,
    val timestamp: Instant = Instant.now()
)
