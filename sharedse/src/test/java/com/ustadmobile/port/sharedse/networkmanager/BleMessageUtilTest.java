package com.ustadmobile.port.sharedse.networkmanager;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * <h1>BleMessageUtilTest</h1>
 *
 * test class which test {{@link BleMessageUtil}} to make sure it behaves as expected when
 * converting List of entries to bytes and vice-versa
 * @author kileha3
 */
public class BleMessageUtilTest {

    @Test
    public void givenListOfEntriesInLong_whenConverted_shouldReturnBytes(){
        List<Long> entryList = Arrays.asList(1056289670L,4590875612L,9076137860L,2912543894L);

        byte[] entriesInBytes = BleMessageUtil.bleMessageLongToBytes(entryList);

        assertTrue("List was converted to bytes ",entriesInBytes.getClass().isArray());
    }

    @Test
    public void givenListOfEntriesInLong_whenConvertedToBytesAndConvertedBackToList_thenShouldTheSame(){
        List<Long> entryList = Arrays.asList(1056289670L,4590875612L,9076137860L,2912543894L,8965341254L);

        byte[] entriesInBytes = BleMessageUtil.bleMessageLongToBytes(entryList);
        List<Long> entriesInListOfLong =  BleMessageUtil.bleMessageBytesToLong(entriesInBytes);

        assertTrue("Lists are equal",entryList.equals(entriesInListOfLong));
    }
}
