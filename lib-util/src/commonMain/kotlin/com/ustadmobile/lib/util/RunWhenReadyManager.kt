package com.ustadmobile.lib.util

/**
 * Simple non-thread safe class that will run a function once it has been marked as ready. If this
 * manager is already marked as ready, the function runs immediately. If not, the function will be
 * executed as soon as it is marked as ready
 */
class RunWhenReadyManager(isReady: Boolean = false) {

    var ready: Boolean = false
        get() = field
        set(value) {
            field = value
            if(value) {
                pendingBlocks.forEach { it.invoke() }
                pendingBlocks.clear()
            }
        }

    init {
        ready = isReady
    }

    private val pendingBlocks = mutableListOf<() -> Unit>()

    fun runWhenReady(block: () -> Unit) {
        if(ready) {
            block.invoke()
        }else {
            pendingBlocks.add(block)
        }
    }

}