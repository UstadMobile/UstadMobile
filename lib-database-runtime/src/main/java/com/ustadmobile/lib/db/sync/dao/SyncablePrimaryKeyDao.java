package com.ustadmobile.lib.db.sync.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.sync.entities.SyncDeviceBits;
import com.ustadmobile.lib.db.sync.entities.SyncablePrimaryKey;

import java.util.Random;

/**
 * Syncable Primary Key DAO supports generation of a syncable primary key. The syncable primary
 * key consists of 32 device bits (stored as a single row in the SyncDeviceBits table) and uses the
 * remaining 32 bits for an auto-increment series.
 */
@UmDao
public abstract class SyncablePrimaryKeyDao  {

    private long deviceBits = -1;

    public long getAndIncrement(int tableId, int increment) {
        if(deviceBits == -1) {
            deviceBits = getDeviceBits();
            if(deviceBits == 0) {
                deviceBits = new Random().nextInt();
                insertDeviceBits(new SyncDeviceBits((int)deviceBits));
            }

            deviceBits <<= 32;
        }


        int nextSequenceNumber = findNextPrimaryKeyByTableId(tableId) + 1;
        if(nextSequenceNumber == 1) {
            insert(new SyncablePrimaryKey(tableId, 1));
        }

        updateNextSequenceNumber(tableId, increment);

        return deviceBits | nextSequenceNumber;
    }

    @UmQuery("SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = :tableId")
    public abstract int findNextPrimaryKeyByTableId(int tableId);

    @UmInsert
    public abstract void insert(SyncablePrimaryKey syncablePrimaryKey);

    @UmQuery("UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + :increment WHERE tableId = :tableId")
    public abstract void updateNextSequenceNumber(int tableId, int increment);

    @UmQuery("SELECT deviceBits FROM SyncDeviceBits WHERE id = " + SyncDeviceBits.PRIMARY_KEY)
    public abstract long getDeviceBits();

    @UmInsert
    public abstract void insertDeviceBits(SyncDeviceBits deviceBits);

}
