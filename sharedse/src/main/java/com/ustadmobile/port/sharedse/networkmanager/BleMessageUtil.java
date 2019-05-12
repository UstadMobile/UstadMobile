package com.ustadmobile.port.sharedse.networkmanager;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Util class for {{@link BleMessage}}, it converts the entry UUID from Long to Bytes and vice versa.
 */
public class BleMessageUtil {

    /**
     * Convert a list of entries in {{@link Long}}  to {{@link Byte}} arrays
     * @param entryList List of entry UUID in {{@link Long}}
     * @return Converted bytes
     */
    public static byte [] bleMessageLongToBytes(List<Long> entryList){
        ByteBuffer buffer = ByteBuffer.allocate(entryList.size() * 8);
        for(long entry: entryList){
            buffer.putLong(entry);
        }
        return buffer.array();
    }

    /**
     * Convert entries from {{@link Byte}} array back to List of {{@link Long}}
     * @param entryInBytes Entry Id' in byte arrays.
     * @return Constructed list of {{@link Long}}
     */
    public static List<Long> bleMessageBytesToLong(byte [] entryInBytes){
        List<Long> entries = new ArrayList<>();
        int BUFFER_SIZE = 8;
        int start = 0;
        for(int position = 0; position < entryInBytes.length/BUFFER_SIZE; position++) {
            int end = start + BUFFER_SIZE;
            long entry = ByteBuffer.wrap(Arrays.copyOfRange(entryInBytes, start, end)).getLong();
            entries.add(entry);
            start += BUFFER_SIZE;
        }
        return entries;

    }

}
