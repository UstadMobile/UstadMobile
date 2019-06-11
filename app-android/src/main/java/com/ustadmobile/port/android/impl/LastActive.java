package com.ustadmobile.port.android.impl;

import java.util.concurrent.atomic.AtomicLong;

//A singleton
public class LastActive {

    //Atomic is thread safe. Can read and write at the same time.
    private AtomicLong lastActive;
    private static final LastActive ourInstance = new LastActive();

    public static LastActive getInstance() {
        return ourInstance;
    }

    private LastActive() {
    }

    public AtomicLong getLastActive() {
        return lastActive;
    }

    public void setLastActive(AtomicLong lastActive) {
        this.lastActive = lastActive;
    }
}
