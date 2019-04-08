package com.`fun`.monkeys.ribeat.or

import android.app.Application
import com.`fun`.monkeys.ribeat.or.data.Order
import io.reactivex.subjects.BehaviorSubject

class OrderController : Application() {
    private var orders = mutableListOf<Order>()
    val ordersChanged = BehaviorSubject.create<List<Order>>().also { it.onNext(orders) }

    fun addOrder(order: Order) {
        orders.add(order)
        ordersChanged.onNext(orders)
    }

    fun resetOrders() {
        orders = mutableListOf()
        ordersChanged.onNext(orders)
    }

    fun removeOrder(order: Order) {
        orders.remove(order)
        ordersChanged.onNext(orders)
    }

    fun getOrderCount(): Int {
        return orders.size
    }

    fun getOrders(): MutableList<Order> {
        return orders
    }

    fun updateOrder(order: Order) {
        ordersChanged.onNext(orders)
    }

    init {
        INSTANCE = this
    }

    companion object {
        lateinit var INSTANCE: OrderController
    }
}