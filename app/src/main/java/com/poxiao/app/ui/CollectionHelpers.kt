package com.poxiao.app.ui

internal fun <T> MutableList<T>.swap(left: Int, right: Int) {
    if (left !in indices || right !in indices || left == right) return
    val temp = this[left]
    this[left] = this[right]
    this[right] = temp
}
