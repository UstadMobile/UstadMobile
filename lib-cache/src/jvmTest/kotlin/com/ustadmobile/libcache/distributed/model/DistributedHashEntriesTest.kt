package com.ustadmobile.libcache.distributed.model

import org.junit.Test
import kotlin.test.assertEquals

class DistributedHashEntriesTest {

    @Test
    fun givenEntriesSerialized_whenDeserialized_thenWillBeEqual() {
        val entries = DistributedHashEntries(
            httpPort = 4243,
            entries = listOf(
                DistributedHashEntry(42L, 43L, 44L)
            )
        )

        val fromSerialized = DistributedHashEntries.fromBytes(entries.toBytes())
        assertEquals(entries, fromSerialized)
    }

}