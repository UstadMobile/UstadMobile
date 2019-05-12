package com.ustadmobile.lib.db;

public class DoorUtils {

    /**
     * Generate the parameters used on Postgres for creation of a sequence based on a set sync
     * device bits prefix
     *
     * @param syncDeviceBits Sync device bits prefix
     *
     * @return String that can be used with postgres ALTER SEQUENCE or CREATE SEQUENCE to set
     * sequence min, max, start and restart parameters
     */
    public static String generatePostgresSyncablePrimaryKeySequenceParameters(int syncDeviceBits) {
        long syncDeviceBitsShifted = ((long)syncDeviceBits) << 32;
        return " INCREMENT BY 1 " +
                " MINVALUE " + syncDeviceBitsShifted +
                " MAXVALUE " + (syncDeviceBitsShifted + 0xFFFFFFFFL) +
                " RESTART WITH " + (syncDeviceBitsShifted + 1) +
                " START WITH " + (syncDeviceBitsShifted + 1);
    }

}
