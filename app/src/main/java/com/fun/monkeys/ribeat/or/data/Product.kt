package com.`fun`.monkeys.ribeat.or.data

import com.`fun`.monkeys.ribeat.or.utils.ApiListManager
import com.`fun`.monkeys.ribeat.or.utils.ApiListManagerImpl
import com.`fun`.monkeys.ribeat.or.utils.resetableLazy
import io.reactivex.Single
import kotlinx.serialization.Serializable

@Serializable
data class Product(val id: Int, val details: String, val name: String, val price: Double, val type: String, val vat: Double, val categoryId: Int, val image: String) {
    object Manager : ApiListManager<Product, Int> by ApiListManagerImpl(
        "http://192.168.86.188:8080/api/v1/products/",
        Product.serializer(),
        emptyList(),
        {
            it.id
        }) {

        private val categoryMapDelegate = resetableLazy {
            val retMap = mutableMapOf<Int, MutableList<Product>>()
            items.forEach {
                if (retMap[it.categoryId] == null) {
                    retMap[it.categoryId] = mutableListOf()
                }
                retMap[it.categoryId]!! += it
            }
            retMap
        }
        val categoryMap by categoryMapDelegate

        override fun fetchItems(): Single<List<Product>> {
            categoryMapDelegate.reset()
            return super.fetchItems()
        }
    }
}