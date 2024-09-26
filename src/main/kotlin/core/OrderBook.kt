package org.iamramtin.core

import org.iamramtin.models.*
import java.util.*
import java.util.Collections.synchronizedList
import java.util.concurrent.ConcurrentSkipListMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Represents an order book for trading operations.
 * Manages buy and sell orders, and executes trades when orders match.
 */
class OrderBook {
    // ConcurrentSkipListMap for thread-safe, sorted storage of orders
    private val buyOrders = ConcurrentSkipListMap<Double, MutableList<Order>>(compareByDescending { it })
    private val sellOrders = ConcurrentSkipListMap<Double, MutableList<Order>>()

    // Thread-safe list to store executed trades
    private val trades = synchronizedList(mutableListOf<Trade>())

    // Atomic counter for generating unique trade IDs
    private val lastTradeId = AtomicLong(0)

    /**
     * Initializes the order book with given data.
     * @param data OrderBookData containing initial bids and asks
     */
    fun initialise(data: OrderBookData) {
        clear()
        addOrders(data.Bids, OrderSide.BUY)
        addOrders(data.Asks, OrderSide.SELL)

        println("Order book updated with snapshot data. ${buyOrders.size} buy orders and ${sellOrders.size} sell orders.")
    }

    /**
     * Retrieves the current state of the order book.
     * @return A map containing lists of bids and asks
     */
    fun getOrderBook(): Map<String, List<Map<String, Any>>> {
        val bids = buyOrders.values.flatten().map { order ->
            mapOf("price" to order.price, "quantity" to order.quantity)
        }

        val asks = sellOrders.values.flatten().map { order ->
            mapOf("price" to order.price, "quantity" to order.quantity)
        }

        return mapOf("bids" to bids, "asks" to asks)
    }

    /**
     * Submits a limit order to the order book.
     * @param order The order to be submitted
     * @return The ID of the submitted order
     */
    fun submitLimitOrder(order: Order): String {
        val matchingOrders = findMatchingOrders(order)
        if (matchingOrders.isEmpty()) addOrder(order) else executeMatches(order, matchingOrders)

        return order.id
    }

    /**
     * Retrieves the most recent trades.
     * @param limit The maximum number of trades to retrieve (default 100)
     * @return List of recent trades, most recent first
     */
    fun getRecentTrades(limit: Int = 100): List<Trade> {
        return trades.takeLast(limit).reversed()
    }

    /**
     * Retrieves a trade by its ID.
     * @param id The ID of the trade to retrieve
     * @return The trade with the given ID, or null if not found
     */
    fun getTrade(id: String): Trade? {
        return trades.find { it.id == id }
    }

    /**
     * Adds multiple orders to the order book.
     * @param orders List of PriceLevel objects containing order information
     * @param orderSide The side (BUY or SELL) of the orders
     */
    private fun addOrders(orders: List<PriceLevel>, orderSide: OrderSide) {
        orders.forEach { priceLevel ->
            val price = priceLevel.Price.toDouble()
            priceLevel.Orders.forEach { orderInfo ->
                addOrder(
                    Order(
                        id = orderInfo.orderId,
                        side = orderSide,
                        price = price,
                        quantity = orderInfo.quantity.toDouble()
                    )
                )
            }
        }
    }

    /**
     * Adds a single order to the appropriate order book (buy or sell).
     * @param order The order to be added
     */
    private fun addOrder(order: Order) {
        if(order.side == OrderSide.BUY) {
            buyOrders.computeIfAbsent(order.price) { synchronizedList(mutableListOf()) }.add(order)
        } else {
            sellOrders.computeIfAbsent(order.price) { synchronizedList(mutableListOf()) }.add(order)
        }
    }

    /**
     * Finds orders that match the given order for potential trades.
     * @param order The order to find matches for
     * @return List of matching orders
     */
    private fun findMatchingOrders(order: Order): List<Order> {
        return if (order.side == OrderSide.BUY) {
            sellOrders.entries
                .takeWhile { (listedPrice, _) -> order.price >= listedPrice }
                .flatMap { it.value }
        } else {
            buyOrders.entries
                .takeWhile { (listedPrice, _) -> order.price <= listedPrice }
                .flatMap { it.value }
        }
    }

    /**
     * Executes trades between the given order and matching orders.
     * @param order The incoming order
     * @param matchingOrders List of orders that match for trading
     */
    private fun executeMatches(order: Order, matchingOrders: List<Order>) {
        var remainingQuantity = order.quantity
        for (matchingOrder in matchingOrders) {
            if (remainingQuantity <= 0) break

            // Determine the quantity and price of the trade based on the incoming order and the matching order
            val tradeQuantity = minOf(remainingQuantity, matchingOrder.quantity)
            val tradePrice = matchingOrder.price

            trades.add(
                Trade(
                    id = "T${lastTradeId.incrementAndGet()}",
                    price = tradePrice,
                    quantity = tradeQuantity,
                    takerSide = order.side
                )
            )

            remainingQuantity -= tradeQuantity
            updateMatchingOrder(matchingOrder, tradeQuantity)
        }

        // If there is remaining quantity in the incoming order, add the remaining order to the order book
        if (remainingQuantity > 0) {
            addOrder(order.copy(quantity = remainingQuantity))
        }
    }

    /**
     * Updates the quantity of a matching order after a trade.
     * @param matchingOrder The order that was matched in a trade
     * @param tradedQuantity The quantity that was traded
     */
    private fun updateMatchingOrder(matchingOrder: Order, tradedQuantity: Double) {
        val orders = if (matchingOrder.side == OrderSide.BUY) buyOrders else sellOrders

        // Retrieve the list of orders at the price level of the matching order
        val orderList = orders[matchingOrder.price]

        // If the order list is not null, update the quantity of the matching order
        orderList?.let {
            matchingOrder.quantity -= tradedQuantity
            if (matchingOrder.quantity <= 0) {
                // If the quantity of the matching order is zero or negative, remove the order from the list
                it.remove(matchingOrder)

                // If the list is empty after removing the order, remove the price level from the order book
                if (it.isEmpty()) {
                    orders.remove(matchingOrder.price)
                }
            }
        }
    }

    /**
     * Clears all orders from the order book.
     */
    private fun clear() {
        buyOrders.clear()
        sellOrders.clear()
    }
}