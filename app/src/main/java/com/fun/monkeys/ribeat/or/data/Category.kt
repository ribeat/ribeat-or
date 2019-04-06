package com.`fun`.monkeys.ribeat.or.data

import com.`fun`.monkeys.ribeat.or.utils.ApiListManager
import com.`fun`.monkeys.ribeat.or.utils.ApiListManagerImpl
import kotlinx.serialization.Serializable

@Serializable
data class Category(val id: Int, val name: String, val deliveryOrder: Int, val image: String) {
    object Manager : ApiListManager<Category, Int> by ApiListManagerImpl("http://192.168.86.153:8080/api/v1/categories/", Category.serializer(), emptyList(), {
        it.id
    })
}