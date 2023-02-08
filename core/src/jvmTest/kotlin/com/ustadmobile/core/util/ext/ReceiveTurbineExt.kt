package com.ustadmobile.core.util.ext

import app.cash.turbine.ReceiveTurbine

suspend fun <T> ReceiveTurbine<T>.awaitItemWhere(
    block: (T) -> Boolean
): T {
    while(true) {
        val item = awaitItem()
        if(block(item))
            return item
    }
}
