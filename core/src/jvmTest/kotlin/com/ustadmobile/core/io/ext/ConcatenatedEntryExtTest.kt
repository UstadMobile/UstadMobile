package com.ustadmobile.core.io.ext

import com.ustadmobile.core.io.ConcatenatedEntry
import com.ustadmobile.door.util.systemTimeInMillis
import org.junit.Assert
import org.junit.Test

class ConcatenatedEntryExtTest {

    @Test
    fun givenConcatenatedEntry_whenSerializedAndDeserialized_thenSHouldBeTheSame() {
        val md5 = ByteArray(16 ){ it.toByte() }
        val concatenatedEntry = ConcatenatedEntry(md5, 1, 1001,
            2000, systemTimeInMillis())
        val serialized = concatenatedEntry.toBytes()
        val deserialized = serialized.toConcatenatedEntry()
        Assert.assertEquals("Deserialized entry = original entry",
            concatenatedEntry, deserialized)
    }

}