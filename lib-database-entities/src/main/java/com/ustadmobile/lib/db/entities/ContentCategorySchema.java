package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

import static com.ustadmobile.lib.db.entities.ContentCategorySchema.TABLE_ID;


/**
 * Represents a schema (list) of categories.
 */
@UmEntity(tableId = TABLE_ID)
public class ContentCategorySchema {

    public static final int TABLE_ID = 2;

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long contentCategorySchemaUid;

    private String schemaName;

    private String schemaUrl;

    @UmSyncLocalChangeSeqNum
    private long contentCategorySchemaLocalChangeSeqNum;

    @UmSyncMasterChangeSeqNum
    private long contentCategorySchemaMasterChangeSeqNum;


    public long getContentCategorySchemaUid() {
        return contentCategorySchemaUid;
    }

    public void setContentCategorySchemaUid(long contentCategorySchemaUid) {
        this.contentCategorySchemaUid = contentCategorySchemaUid;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getSchemaUrl() {
        return schemaUrl;
    }

    public void setSchemaUrl(String schemaUrl) {
        this.schemaUrl = schemaUrl;
    }

    public long getContentCategorySchemaLocalChangeSeqNum() {
        return contentCategorySchemaLocalChangeSeqNum;
    }

    public void setContentCategorySchemaLocalChangeSeqNum(long contentCategorySchemaLocalChangeSeqNum) {
        this.contentCategorySchemaLocalChangeSeqNum = contentCategorySchemaLocalChangeSeqNum;
    }

    public long getContentCategorySchemaMasterChangeSeqNum() {
        return contentCategorySchemaMasterChangeSeqNum;
    }

    public void setContentCategorySchemaMasterChangeSeqNum(long contentCategorySchemaMasterChangeSeqNum) {
        this.contentCategorySchemaMasterChangeSeqNum = contentCategorySchemaMasterChangeSeqNum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ContentCategorySchema schema = (ContentCategorySchema) o;

        if (contentCategorySchemaUid != schema.contentCategorySchemaUid) return false;
        if (schemaName != null ? !schemaName.equals(schema.schemaName) : schema.schemaName != null)
            return false;
        return schemaUrl != null ? schemaUrl.equals(schema.schemaUrl) : schema.schemaUrl == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (contentCategorySchemaUid ^ (contentCategorySchemaUid >>> 32));
        result = 31 * result + (schemaName != null ? schemaName.hashCode() : 0);
        result = 31 * result + (schemaUrl != null ? schemaUrl.hashCode() : 0);
        return result;
    }
}
