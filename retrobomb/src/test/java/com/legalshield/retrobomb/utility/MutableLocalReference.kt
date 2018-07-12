package com.legalshield.retrobomb.utility

data class MutableLocalReference<T>(private var value: T? = null) {
    fun get() = value
    fun set(value: T) {
        this.value = value
    }
}