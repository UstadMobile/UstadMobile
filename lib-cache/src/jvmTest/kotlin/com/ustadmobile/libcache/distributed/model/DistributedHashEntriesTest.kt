package com.ustadmobile.libcache.distributed.model

import org.junit.Test
import kotlin.test.assertEquals

class DistributedHashEntriesTest {

    @Test
    fun givenEntriesSerialized_whenDeserialized_thenWillBeEqual() {
        val entries = DistributedHashEntries(
            httpPort = 4243,
            entries = listOf(
                DistributedHashCacheEntry(42L, 43L, 44L)
            )
        )

        val fromSerialized = DistributedCachePacket.fromBytes(entries.toBytes())
        assertEquals(entries, fromSerialized)
    }

    @Test
    fun givenPingSerialized_whenDeserialized_thenWillBeEqual() {
        val ping = DistributedCachePing(
            id = 42L,
            payload = "payload".toByteArray()
        )

        val fromSerialized = DistributedCachePacket.fromBytes(ping.toBytes())
        assertEquals(ping, fromSerialized)
    }


}