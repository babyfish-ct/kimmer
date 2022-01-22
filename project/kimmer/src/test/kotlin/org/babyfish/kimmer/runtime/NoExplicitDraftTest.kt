package org.babyfish.kimmer.runtime

import kotlinx.coroutines.runBlocking
import org.babyfish.kimmer.*
import kotlin.test.Test
import kotlin.test.expect

/*
 * graphql-provider should work even if user have not generated draft codes by ksp.
 */
class NoExplicitDraftTest {

    @Test
    fun testOneToMany() {

        val json1 = """{"items":[{"product":"BMW"}],"no":"order"}"""
        val json2 = """{"items":[{"product":"BMW!"}],"no":"order!"}"""

        val order = produce(Order::class) {
            Draft.set(this, Order::no, "order")
            (Draft.getOrCreate(this, Order::items) as MutableList<SyncDraft<OrderItem>>).add(
                produceDraft(OrderItem::class) {
                    Draft.set(this, OrderItem::product, "BMW")
                }
            )
        }
        expect(json1) {
            order.toString()
        }
        expect(order) {
            Immutable.fromString(json1, Order::class)
        }
        val order2 = produce(Order::class, order) {
            Draft.set(this, Order::no, Draft.get(this, Order::no) + "!")
            (Draft.get(this, Order::items) as MutableList<Draft<OrderItem>>).let {
                for (item in it) {
                    Draft.set(item, OrderItem::product, Draft.get(item, OrderItem::product) + "!")
                }
            }
        }
        expect(json2) {
            order2.toString()
        }
        expect(order2) {
            Immutable.fromString(json2, Order::class)
        }
    }

    @Test
    fun testManyToOne() {

        val json1 = """{"order":{"no":"order"},"product":"BMW"}"""
        val json2 = """{"order":{"no":"order!"},"product":"BMW!"}"""

        val orderItem = produce(OrderItem::class) {
            Draft.set(this, OrderItem::product, "BMW")
            Draft.set(
                (Draft.getOrCreate(this, OrderItem::order) as SyncDraft<Order>),
                Order::no,
                "order"
            )
        }
        expect(json1) {
            orderItem.toString()
        }
        expect(orderItem) {
            Immutable.fromString(json1, OrderItem::class)
        }

        val orderItem2 = produce(OrderItem::class, orderItem) {
            Draft.set(this, OrderItem::product, Draft.get(this, OrderItem::product) + "!")
            val order = (Draft.get(this, OrderItem::order) as SyncDraft<Order>)
            Draft.set(
                (Draft.getOrCreate(this, OrderItem::order) as SyncDraft<Order>),
                Order::no,
                Draft.get(order, Order::no) + "!"
            )
        }
        expect(json2) {
            orderItem2.toString()
        }
        expect(orderItem2) {
            Immutable.fromString(json2, OrderItem::class)
        }
    }

    @Test
    fun testOneToManyAsync() {

        val json1 = """{"items":[{"product":"BMW"}],"no":"order"}"""
        val json2 = """{"items":[{"product":"BMW!"}],"no":"order!"}"""

        runBlocking {
            val order = produceAsync(Order::class) {
                Draft.set(this, Order::no, "order")
                (Draft.getOrCreate(this, Order::items) as MutableList<AsyncDraft<OrderItem>>).add(
                    produceDraftAsync(OrderItem::class) {
                        Draft.set(this, OrderItem::product, "BMW")
                    }
                )
            }
            expect(json1) {
                order.toString()
            }
            expect(order) {
                Immutable.fromString(json1, Order::class)
            }
            val order2 = produceAsync(Order::class, order) {
                Draft.set(this, Order::no, Draft.get(this, Order::no) + "!")
                (Draft.get(this, Order::items) as MutableList<Draft<OrderItem>>).let {
                    for (item in it) {
                        Draft.set(item, OrderItem::product, Draft.get(item, OrderItem::product) + "!")
                    }
                }
            }
            expect(json2) {
                order2.toString()
            }
            expect(order2) {
                Immutable.fromString(json2, Order::class)
            }
        }
    }

    @Test
    fun testManyToOneAsync() {

        val json1 = """{"order":{"no":"order"},"product":"BMW"}"""
        val json2 = """{"order":{"no":"order!"},"product":"BMW!"}"""

        runBlocking {
            val orderItem = produceAsync(OrderItem::class) {
                Draft.set(this, OrderItem::product, "BMW")
                Draft.set(
                    (Draft.getOrCreate(this, OrderItem::order) as AsyncDraft<Order>),
                    Order::no,
                    "order"
                )
            }
            expect(json1) {
                orderItem.toString()
            }
            expect(orderItem) {
                Immutable.fromString(json1, OrderItem::class)
            }

            val orderItem2 = produceAsync(OrderItem::class, orderItem) {
                Draft.set(this, OrderItem::product, Draft.get(this, OrderItem::product) + "!")
                val order = (Draft.get(this, OrderItem::order) as AsyncDraft<Order>)
                Draft.set(
                    order,
                    Order::no,
                    Draft.get(order, Order::no) + "!"
                )
            }
            expect(json2) {
                orderItem2.toString()
            }
            expect(orderItem2) {
                Immutable.fromString(json2, OrderItem::class)
            }
        }
    }

    interface Order: Immutable {
        val no: String
        val items: List<OrderItem>
    }

    interface OrderItem: Immutable {
        val product: String
        val order: Order
    }
}