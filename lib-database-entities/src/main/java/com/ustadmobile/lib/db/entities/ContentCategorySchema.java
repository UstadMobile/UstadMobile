package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

/**
 * Represents a schema (list) of categories.
 */
@UmEntity
public class ContentCategorySchema {

    @UmPrimaryKey(autoIncrement = true)
    private long contentCategorySchemaUid;

    private String schemaName;

    private String schemaUrl;

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
}
