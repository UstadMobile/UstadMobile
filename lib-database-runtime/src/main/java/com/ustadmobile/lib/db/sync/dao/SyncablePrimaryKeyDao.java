package com.ustadmobile.lib.db.sync.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmOnConflictStrategy;
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

    private volatile int deviceBits = -1;

    private long deviceMask = -1L;


    public synchronized long getAndIncrement(int tableId, int increment) {
        if(deviceMask== -1) {
            deviceBits = getDeviceBits();
            deviceMask = ((long)deviceBits << 32);
        }


        int nextSequenceNumber = findNextPrimaryKeyByTableId(tableId) + 1;
        if(nextSequenceNumber == 1) {
            insert(new SyncablePrimaryKey(tableId, 1));
        }

        updateNextSequenceNumber(tableId, increment);

        return deviceMask | nextSequenceNumber;
    }

    @UmQuery("SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = :tableId")
    public abstract int findNextPrimaryKeyByTableId(int tableId);

    @UmInsert
    public abstract void insert(SyncablePrimaryKey syncablePrimaryKey);

    @UmQuery("UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + :increment WHERE tableId = :tableId")
    public abstract void updateNextSequenceNumber(int tableId, int increment);


    public synchronized int getDeviceBits() {
        if(deviceBits == -1) {
            deviceBits = selectDeviceBits();
            if(deviceBits == 0) {
                deviceBits = new Random().nextInt();
                insertDeviceBits(new SyncDeviceBits((int)deviceBits));
            }
        }

        return deviceBits;
    }

    public synchronized void invalidateDeviceBits() {
        deviceBits = -1;
        deviceMask = -1L;
    }

    @UmQuery("SELECT deviceBits FROM SyncDeviceBits WHERE id = " + SyncDeviceBits.PRIMARY_KEY)
    protected abstract int selectDeviceBits();

    @UmInsert(onConflict = UmOnConflictStrategy.REPLACE)
    public abstract void insertDeviceBits(SyncDeviceBits deviceBits);

}
