package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

import static com.ustadmobile.lib.db.entities.XObjectEntity.TABLE_ID;

@UmEntity(tableId = TABLE_ID)
public class XObjectEntity {

    public static final int TABLE_ID = 54;

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long XObjectUid;

    private String objectType;

    private String objectId;

    private String definitionType;

    private String interactionType;

    private String correctResponsePattern;

    @UmSyncMasterChangeSeqNum
    private long XObjectMasterChangeSeqNum;

    @UmSyncLocalChangeSeqNum
    private long XObjectocalChangeSeqNum;

    @UmSyncLastChangedBy
    private int XObjectLastChangedBy;

    public long getXObjectUid() {
        return XObjectUid;
    }

    public void setXObjectUid(long XObjectUid) {
        this.XObjectUid = XObjectUid;
    }

    public long getXObjectMasterChangeSeqNum() {
        return XObjectMasterChangeSeqNum;
    }

    public void setXObjectMasterChangeSeqNum(long XObjectMasterChangeSeqNum) {
        this.XObjectMasterChangeSeqNum = XObjectMasterChangeSeqNum;
    }

    public long getXObjectocalChangeSeqNum() {
        return XObjectocalChangeSeqNum;
    }

    public void setXObjectocalChangeSeqNum(long XObjectocalChangeSeqNum) {
        this.XObjectocalChangeSeqNum = XObjectocalChangeSeqNum;
    }

    public int getXObjectLastChangedBy() {
        return XObjectLastChangedBy;
    }

    public void setXObjectLastChangedBy(int XObjectLastChangedBy) {
        this.XObjectLastChangedBy = XObjectLastChangedBy;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getDefinitionType() {
        return definitionType;
    }

    public void setDefinitionType(String definitionType) {
        this.definitionType = definitionType;
    }

    public String getInteractionType() {
        return interactionType;
    }

    public void setInteractionType(String interactionType) {
        this.interactionType = interactionType;
    }

    public String getCorrectResponsePattern() {
        return correctResponsePattern;
    }

    public void setCorrectResponsePattern(String correctResponsePattern) {
        this.correctResponsePattern = correctResponsePattern;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        XObjectEntity that = (XObjectEntity) o;

        if (XObjectUid != that.XObjectUid) return false;
        if (objectType != null ? !objectType.equals(that.objectType) : that.objectType != null)
            return false;
        if (objectId != null ? !objectId.equals(that.objectId) : that.objectId != null)
            return false;
        if (definitionType != null ? !definitionType.equals(that.definitionType) : that.definitionType != null)
            return false;
        if (interactionType != null ? !interactionType.equals(that.interactionType) : that.interactionType != null)
            return false;
        return correctResponsePattern != null ? correctResponsePattern.equals(that.correctResponsePattern) : that.correctResponsePattern == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (XObjectUid ^ (XObjectUid >>> 32));
        result = 31 * result + (objectType != null ? objectType.hashCode() : 0);
        result = 31 * result + (objectId != null ? objectId.hashCode() : 0);
        result = 31 * result + (definitionType != null ? definitionType.hashCode() : 0);
        result = 31 * result + (interactionType != null ? interactionType.hashCode() : 0);
        result = 31 * result + (correctResponsePattern != null ? correctResponsePattern.hashCode() : 0);
        return result;
    }
}

