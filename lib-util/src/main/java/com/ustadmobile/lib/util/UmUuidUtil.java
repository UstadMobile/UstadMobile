package com.ustadmobile.lib.util;

import com.github.fzakaria.ascii85.Ascii85;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

/**
 * Brief UUID utils
 */
public class UmUuidUtil {

    /**
     * Encode a UUID as a string using Ascii85. Ascii85 adds 1/4 extra bytes, thus it's slightly
     * more efficient than base64 (which has a 1/3 overhead). This results in a 20byte string for
     * a UUID (vs. 36 bytes in the default toString implementation of uuid, or 32 bytes if the dashes
     * are removed)
     *
     * @param uuid UUID object
     * @return
     */
    public static final String encodeUuidWithAscii85(UUID uuid) {
        byte[] buf = new byte[16];
        ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN)
                .putLong(uuid.getMostSignificantBits())
                .putLong(uuid.getLeastSignificantBits());
        return Ascii85.encode(buf);
    }

}
