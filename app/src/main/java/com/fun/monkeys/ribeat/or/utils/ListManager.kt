package com.`fun`.monkeys.ribeat.or.utils

interface ListManager<T> {
    var items: List<T>

    val count : Int
        get() = items.size

}

class ListManagerImpl<T>(override var items: List<T>) : ListManager<T>