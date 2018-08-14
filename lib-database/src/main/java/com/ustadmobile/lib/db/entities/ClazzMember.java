package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

/**
 * This class mediates the relationship between a person and a clazz. A member can be a teacher,
 * or a student. Each member has a joining date, and a leaving date.
 */
@UmEntity
public class ClazzMember implements SyncableEntity {

    public static final int ROLE_STUDENT = 1;

    public static final int ROLE_TEACHER = 2;

    @UmPrimaryKey(autoIncrement = true)
    private long clazzMemberUid;

    private long clazzMemberPersonUid;

    private long dateJoined;

    private long dateLeft;

    private int role;

    private long masterChangeSeqNum;

    private long localChangeSeqNum;

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
}
