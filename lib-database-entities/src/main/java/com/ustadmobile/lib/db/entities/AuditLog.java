/* GENERATED FILE : DO NOT EDIT */

package com.ustadmobile.lib.db.entities;


import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

@UmEntity(tableId = 53)
public class AuditLog {
    @UmPrimaryKey(autoGenerateSyncable = true)
    private long auditLogUid;

    @UmSyncMasterChangeSeqNum
    private long auditLogMasterChangeSeqNum;

    @UmSyncLocalChangeSeqNum
    private long auditLogLocalChangeSeqNum;

    @UmSyncLastChangedBy
    private int auditLogLastChangedBy;

    private long auditLogActorPersonUid;

    private int auditLogTableUid;

    private long auditLogEntityUid;

    private long auditLogDate;

    private String notes;

    public AuditLog(long personUid, int table, long entityUid){
        this.auditLogActorPersonUid = personUid;
        this.auditLogTableUid = table;
        this.auditLogEntityUid = entityUid;
        this.auditLogDate = System.currentTimeMillis();
    }

    public AuditLog(){
        this.auditLogDate = System.currentTimeMillis();
    }

    public long getAuditLogUid() {
        return auditLogUid;
    }

    public void setAuditLogUid(long auditLogUid) {
        this.auditLogUid = auditLogUid;
    }

    public long getAuditLogMasterChangeSeqNum() {
        return auditLogMasterChangeSeqNum;
    }

    public void setAuditLogMasterChangeSeqNum(long auditLogMasterChangeSeqNum) {
        this.auditLogMasterChangeSeqNum = auditLogMasterChangeSeqNum;
    }

    public long getAuditLogLocalChangeSeqNum() {
        return auditLogLocalChangeSeqNum;
    }

    public void setAuditLogLocalChangeSeqNum(long auditLogLocalChangeSeqNum) {
        this.auditLogLocalChangeSeqNum = auditLogLocalChangeSeqNum;
    }

    public int getAuditLogLastChangedBy() {
        return auditLogLastChangedBy;
    }

    public void setAuditLogLastChangedBy(int auditLogLastChangedBy) {
        this.auditLogLastChangedBy = auditLogLastChangedBy;
    }

    public long getAuditLogActorPersonUid() {
        return auditLogActorPersonUid;
    }

    public void setAuditLogActorPersonUid(long auditLogActorPersonUid) {
        this.auditLogActorPersonUid = auditLogActorPersonUid;
    }

    public int getAuditLogTableUid() {
        return auditLogTableUid;
    }

    public void setAuditLogTableUid(int auditLogTableUid) {
        this.auditLogTableUid = auditLogTableUid;
    }

    public long getAuditLogEntityUid() {
        return auditLogEntityUid;
    }

    public void setAuditLogEntityUid(long auditLogEntityUid) {
        this.auditLogEntityUid = auditLogEntityUid;
    }

    public long getAuditLogDate() {
        return auditLogDate;
    }

    public void setAuditLogDate(long auditLogDate) {
        this.auditLogDate = auditLogDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
