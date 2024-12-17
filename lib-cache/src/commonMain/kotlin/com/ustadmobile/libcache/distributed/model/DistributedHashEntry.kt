package com.ustadmobile.libcache.distributed.model

data class DistributedHashEntry(
    val version: Byte,
    val urlHash: Long,
)
