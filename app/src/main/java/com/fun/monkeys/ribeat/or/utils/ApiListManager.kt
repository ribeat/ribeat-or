package com.`fun`.monkeys.ribeat.or.utils

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.rx.rxResponseObject
import com.github.kittinunf.fuel.serialization.kotlinxDeserializerOf
import io.reactivex.Single
import kotlinx.serialization.KSerializer
import kotlinx.serialization.list

interface ApiListManager<T, K>: ListManager<T> {
    val apiUrl: String
    val serializer: KSerializer<T>
    val itemsMap: Map<K, T>

    fun fetchItems(): Single<List<T>> {
        println("$origin$apiUrl")
        return "$origin$apiUrl".httpGet().rxResponseObject(kotlinxDeserializerOf(serializer.list)).map {
            items = it
            it
        }
    }

    operator fun get(id: K) = itemsMap[id]

    companion object {
        const val origin = "http://10.3.1.165:8080/"
    }
}

class ApiListManagerImpl<T, K>(
    override val apiUrl: String,
    override val serializer: KSerializer<T>,
    override var items: List<T>,
    val getKey: (T) -> K
) : ApiListManager<T, K> {
    private val itemsMapDelegate = resetableLazy {
        items.map { getKey(it) to it }.toMap()
    }
    override val itemsMap by itemsMapDelegate


    override fun fetchItems(): Single<List<T>> {
        itemsMapDelegate.reset()
        return super.fetchItems()
    }
}