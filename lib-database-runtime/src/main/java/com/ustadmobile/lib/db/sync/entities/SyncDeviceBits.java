package com.ustadmobile.lib.db.sync.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

import java.util.Random;

@UmEntity
@Entity
public class SyncDeviceBits {

    public static final int PRIMARY_KEY = 1;

    @UmPrimaryKey
    @PrimaryKey
    private int id;

    private int deviceBits;

    private boolean master;

    public SyncDeviceBits() {

    }

    public static SyncDeviceBits newRandomInstance() {
        return new SyncDeviceBits(new Random().nextInt());
    }

    public SyncDeviceBits(int deviceBits) {
        this.id = PRIMARY_KEY;
        this.deviceBits = deviceBits;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDeviceBits() {
        return deviceBits;
    }

    public void setDeviceBits(int deviceBits) {
        this.deviceBits = deviceBits;
    }


    public boolean isMaster() {
        return master;
    }

    public void setMaster(boolean master) {
        this.master = master;
    }
}
