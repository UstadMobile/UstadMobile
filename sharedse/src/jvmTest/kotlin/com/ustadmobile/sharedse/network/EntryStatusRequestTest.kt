package com.ustadmobile.sharedse.network

import org.junit.Assert
import org.junit.Test

class EntryStatusRequestTest {

    @Test
    fun givenEntryStatus_whenConvertedToFromBytes_thenShouldBeTheSame() {
        val originalStatus = EntryStatusRequest("http://localhost/", longArrayOf(42L, 6L, 7L))
        val statusBytes = originalStatus.toBytes()
        val fromBytes = EntryStatusRequest.fromBytes(statusBytes)
        Assert.assertEquals("Original request is the same as request created from bytes",
            originalStatus, fromBytes)
    }

}