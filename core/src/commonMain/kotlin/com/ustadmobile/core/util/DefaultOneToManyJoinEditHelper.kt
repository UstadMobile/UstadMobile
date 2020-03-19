package com.ustadmobile.core.util

import kotlinx.atomicfu.atomic

class DefaultOneToManyJoinEditHelper<T>(pkGetter: (T) -> Long,  pkSetter: T.(Long) -> Unit)
    : OneToManyJoinEditHelper<T, Long>(pkGetter, pkSetter, {-1L}) {

    private val atomicLong = atomic(0L)

    override val fakePkGenerator: () -> Long
        get() = { atomicLong.decrementAndGet() }

}