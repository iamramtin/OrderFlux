package core

import org.iamramtin.core.OrderBook
import org.iamramtin.models.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class OrderBookTest {
    private lateinit var orderBook: OrderBook

    @BeforeEach
    fun setup() {
        orderBook = OrderBook()
    }

    @Test
    fun `test initialise order book`() {
        val data = OrderBookData(
            LastChange = 1234567890,
            Asks = listOf(
                PriceLevel("100.0", listOf(OrderInfo("1", "10.0"))),
                PriceLevel("101.0", listOf(OrderInfo("2", "5.0")))
            ),
            Bids = listOf(
                PriceLevel("99.0", listOf(OrderInfo("3", "8.0"))),
                PriceLevel("98.0", listOf(OrderInfo("4", "12.0")))
            ),
            SequenceNumber = 1,
            Checksum = 12345
        )

        orderBook.initialise(data)

        val book = orderBook.getOrderBook()
        assertEquals(2, book["asks"]?.size)
        assertEquals(2, book["bids"]?.size)
    }

    @Test
    fun `test submit buy limit order`() {
        val order = Order(id = "5", side = OrderSide.BUY, price = 100.0, quantity = 5.0)
        val orderId = orderBook.submitLimitOrder(order)

        assertEquals("5", orderId)
        val book = orderBook.getOrderBook()
        assertEquals(1, book["bids"]?.size)
        assertEquals(0, book["asks"]?.size)
    }

    @Test
    fun `test submit sell limit order`() {
        val order = Order(id = "6", side = OrderSide.SELL, price = 100.0, quantity = 5.0)
        val orderId = orderBook.submitLimitOrder(order)

        assertEquals("6", orderId)
        val book = orderBook.getOrderBook()
        assertEquals(0, book["bids"]?.size)
        assertEquals(1, book["asks"]?.size)
    }

    @Test
    fun `test matching orders`() {
        orderBook.submitLimitOrder(Order(id = "7", side = OrderSide.BUY, price = 100.0, quantity = 10.0))
        orderBook.submitLimitOrder(Order(id = "8", side = OrderSide.SELL, price = 100.0, quantity = 5.0))

        val book = orderBook.getOrderBook()
        assertEquals(1, book["bids"]?.size)
        assertEquals(0, book["asks"]?.size)
        assertEquals(5.0, (book["bids"]?.get(0) as Map<*, *>)["quantity"])

        val trades = orderBook.getRecentTrades()
        assertEquals(1, trades.size)
        assertEquals(5.0, trades[0].quantity)
        assertEquals(100.0, trades[0].price)
    }

    @Test
    fun `test partial order execution`() {
        orderBook.submitLimitOrder(Order(id = "9", side = OrderSide.BUY, price = 100.0, quantity = 10.0))
        orderBook.submitLimitOrder(Order(id = "10", side = OrderSide.SELL, price = 100.0, quantity = 15.0))

        val book = orderBook.getOrderBook()
        assertEquals(0, book["bids"]?.size)
        assertEquals(1, book["asks"]?.size)
        assertEquals(5.0, (book["asks"]?.get(0) as Map<*, *>)["quantity"])

        val trades = orderBook.getRecentTrades()
        assertEquals(1, trades.size)
        assertEquals(10.0, trades[0].quantity)
        assertEquals(100.0, trades[0].price)
    }

    @Test
    fun `test get recent trades with limit`() {
        for (i in 1..20) {
            orderBook.submitLimitOrder(Order(id = i.toString(), side = OrderSide.BUY, price = 100.0, quantity = 1.0))
            orderBook.submitLimitOrder(Order(id = (i + 100).toString(), side = OrderSide.SELL, price = 100.0, quantity = 1.0))
        }

        val trades = orderBook.getRecentTrades(5)
        assertEquals(5, trades.size)
        assertTrue(trades[0].id > trades[4].id) // Ensure most recent first
    }

    @Test
    fun `test order book sorting`() {
        orderBook.submitLimitOrder(Order(id = "11", side = OrderSide.BUY, price = 98.0, quantity = 5.0))
        orderBook.submitLimitOrder(Order(id = "12", side = OrderSide.BUY, price = 99.0, quantity = 5.0))
        orderBook.submitLimitOrder(Order(id = "13", side = OrderSide.SELL, price = 101.0, quantity = 5.0))
        orderBook.submitLimitOrder(Order(id = "14", side = OrderSide.SELL, price = 100.0, quantity = 5.0))

        val book = orderBook.getOrderBook()
        val bids = book["bids"] as List<Map<String, Any>>
        val asks = book["asks"] as List<Map<String, Any>>

        assertEquals(99.0, bids[0]["price"])
        assertEquals(98.0, bids[1]["price"])
        assertEquals(100.0, asks[0]["price"])
        assertEquals(101.0, asks[1]["price"])
    }
}