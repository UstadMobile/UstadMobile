package com.ustadmobile.sharedse.network

import com.ustadmobile.sharedse.network.BleMessageUtil.bleMessageLongToBytes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.*

/**
 * Test class which test {[BleMessageUtil]} to make sure it behaves as expected when
 * converting List of entries to bytes and vice-versa
 *
 * @author kileha3
 */
class BleMessageUtilTest {

    private val entryList = Arrays.asList(1056289670L, 4590875612L, 9076137860L, 2912543894L, 8965341254L)


    @Test
    fun givenListOfEntriesInLong_whenConverted_shouldReturnBytes() {

        val entriesInBytes = bleMessageLongToBytes(entryList)

        assertTrue("Entry list was converted to bytes ", entriesInBytes.javaClass.isArray)
    }

    @Test
    fun givenListOfEntriesInLong_whenConvertedToBytesAndConvertedBackToList_thenShouldTheSame() {

        val entriesInBytes = bleMessageLongToBytes(entryList)
        val entriesInListOfLong = BleMessageUtil.bleMessageBytesToLong(entriesInBytes)

        assertEquals("Entry lists before and after conversions are equal", entryList,
                entriesInListOfLong)
    }

}
