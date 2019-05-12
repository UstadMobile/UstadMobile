package com.ustadmobile.lib.db;

public abstract class UmDbMigration {

    private int fromVersion;

    private int toVersion;

    public UmDbMigration(int fromVersion, int toVersion) {
        this.fromVersion = fromVersion;
        this.toVersion = toVersion;
    }

    public int getFromVersion() {
        return fromVersion;
    }

    public int getToVersion() {
        return toVersion;
    }

    public abstract void migrate(DoorDbAdapter db);

}
