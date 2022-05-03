package com.ustadmobile.core.util.ext

fun <T> MutableList<T>.addSafelyToPosition(position: Int, content: T){
    when {
        position < 0 -> {
            add(0, content)
        }
        position > size -> {
            add(content)
        }
        else -> {
            add(position, content)
        }
    }
}

fun <T> MutableList<T>.addAllSafelyToPosition(position: Int, content: Collection<T>){
    when {
        position < 0 -> {
            addAll(0, content)
        }
        position > size -> {
            addAll(content)
        }
        else -> {
            addAll(position, content)
        }
    }
}