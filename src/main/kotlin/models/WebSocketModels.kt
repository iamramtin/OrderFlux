package org.iamramtin.models

import kotlinx.serialization.Serializable

@Serializable
data class OrderBookUpdateResponse(
    val type: String,
    val currencyPairSymbol: String,
    val data: OrderBookData
)

@Serializable
data class OrderBookData(
    val LastChange: Long,
    val Asks: List<PriceLevel>,
    val Bids: List<PriceLevel>,
    val SequenceNumber: Long,
    val Checksum: Long
)

@Serializable
data class PriceLevel(
    val Price: String,
    val Orders: List<OrderInfo>
)

@Serializable
data class OrderInfo(
    val orderId: String,
    val quantity: String
)
