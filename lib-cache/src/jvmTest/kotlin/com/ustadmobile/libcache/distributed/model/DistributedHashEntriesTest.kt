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
            id = 42,
            deviceName = "device",
            httpPort = 8082,
            payload = "payload".toByteArray()
        )

        val serialized = ping.toBytes()
        val fromSerialized = DistributedCachePacket.fromBytes(serialized)
        assertEquals(ping, fromSerialized)
    }


}