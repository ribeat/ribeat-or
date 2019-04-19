package com.`fun`.monkeys.ribeat.or.data

import com.`fun`.monkeys.ribeat.or.utils.ApiListManager
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.rx.rxResponse
import io.reactivex.Single
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.list

@Serializable
data class Order(var quantity: Int, var productId: Int, var baypassDeliveryOrder: Boolean) {
    @Serializable
    data class OrderPostData(val orderItems: List<Order>, val table: String)

    object Manager {
        fun sendOrders(orders: List<Order>): Single<ByteArray> {
            return "${ApiListManager.origin}api/v1/orders/".httpPost()
                .body(Json.stringify(OrderPostData.serializer(), OrderPostData(orders, (1 until 9).random().toString())))
                .rxResponse()
        }
    }
}