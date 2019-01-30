package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

@UmEntity(tableId = 48)
public class PersonLocationJoin {

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long personLocationUid;

    private long personLocationPersonUid;

    private long personLocationLocationUid;

    @UmSyncMasterChangeSeqNum
    private long plMasterCsn;

    @UmSyncLocalChangeSeqNum
    private long plLocalCsn;

    @UmSyncLastChangedBy
    private int plLastChangedBy;

    public PersonLocationJoin() {

    }

    public PersonLocationJoin(Person person, Location location) {
        this.personLocationPersonUid = person.getPersonUid();
        this.personLocationLocationUid = location.getLocationUid();
    }

    public long getPersonLocationUid() {
        return personLocationUid;
    }

    public void setPersonLocationUid(long personLocationUid) {
        this.personLocationUid = personLocationUid;
    }

    public long getPersonLocationPersonUid() {
        return personLocationPersonUid;
    }

    public void setPersonLocationPersonUid(long personLocationPersonUid) {
        this.personLocationPersonUid = personLocationPersonUid;
    }

    public long getPersonLocationLocationUid() {
        return personLocationLocationUid;
    }

    public void setPersonLocationLocationUid(long personLocationLocationUid) {
        this.personLocationLocationUid = personLocationLocationUid;
    }

    public long getPlMasterCsn() {
        return plMasterCsn;
    }

    public void setPlMasterCsn(long plMasterCsn) {
        this.plMasterCsn = plMasterCsn;
    }

    public long getPlLocalCsn() {
        return plLocalCsn;
    }

    public void setPlLocalCsn(long plLocalCsn) {
        this.plLocalCsn = plLocalCsn;
    }

    public int getPlLastChangedBy() {
        return plLastChangedBy;
    }

    public void setPlLastChangedBy(int plLastChangedBy) {
        this.plLastChangedBy = plLastChangedBy;
    }
}
