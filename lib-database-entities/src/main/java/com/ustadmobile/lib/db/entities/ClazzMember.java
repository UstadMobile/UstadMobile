package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

/**
 * This class mediates the relationship between a person and a clazz. A member can be a teacher,
 * or a student. Each member has a joining date, and a leaving date.
 */
@UmEntity(tableId = 7)
public class ClazzMember implements SyncableEntity {

    public static final int ROLE_STUDENT = 1;

    public static final int ROLE_TEACHER = 2;

    @UmPrimaryKey(autoIncrement = true)
    private long clazzMemberUid;

    private long clazzMemberPersonUid;

    private long clazzMemberClazzUid;

    private long dateJoined;

    private long dateLeft;

    private int role;

    private long masterChangeSeqNum;

    private long localChangeSeqNum;

    @UmSyncLocalChangeSeqNum
    private long clazzMemberLocalChangeSeqNum;

    @UmSyncMasterChangeSeqNum
    private long clazzMemberMasterChangeSeqNum;

    public long getClazzMemberUid() {
        return clazzMemberUid;
    }

    /**
     * The personUid field of the related Person entity
     *
     * @param clazzMemberUid
     */
    public void setClazzMemberUid(long clazzMemberUid) {
        this.clazzMemberUid = clazzMemberUid;
    }

    public long getClazzMemberPersonUid() {
        return clazzMemberPersonUid;
    }

    public void setClazzMemberPersonUid(long clazzMemberPersonUid) {
        this.clazzMemberPersonUid = clazzMemberPersonUid;
    }

    public long getDateJoined() {
        return dateJoined;
    }

    public void setDateJoined(long dateJoined) {
        this.dateJoined = dateJoined;
    }

    public long getDateLeft() {
        return dateLeft;
    }

    public void setDateLeft(long dateLeft) {
        this.dateLeft = dateLeft;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public long getClazzMemberClazzUid() {
        return clazzMemberClazzUid;
    }

    public void setClazzMemberClazzUid(long clazzMemberClazzUid) {
        this.clazzMemberClazzUid = clazzMemberClazzUid;
    }

    @Override
    public long getMasterChangeSeqNum() {
        return masterChangeSeqNum;
    }

    @Override
    public void setMasterChangeSeqNum(long masterChangeSeqNum) {
        this.masterChangeSeqNum = masterChangeSeqNum;
    }

    @Override
    public long getLocalChangeSeqNum() {
        return localChangeSeqNum;
    }

    @Override
    public void setLocalChangeSeqNum(long localChangeSeqNum) {
        this.localChangeSeqNum = localChangeSeqNum;
    }

    public long getClazzMemberLocalChangeSeqNum() {
        return clazzMemberLocalChangeSeqNum;
    }

    public void setClazzMemberLocalChangeSeqNum(long clazzMemberLocalChangeSeqNum) {
        this.clazzMemberLocalChangeSeqNum = clazzMemberLocalChangeSeqNum;
    }

    public long getClazzMemberMasterChangeSeqNum() {
        return clazzMemberMasterChangeSeqNum;
    }

    public void setClazzMemberMasterChangeSeqNum(long clazzMemberMasterChangeSeqNum) {
        this.clazzMemberMasterChangeSeqNum = clazzMemberMasterChangeSeqNum;
    }
}
