package com.ustadmobile.lib.db.entities;

public class DistinctCategorySchema {

    private long contentCategoryUid;

    private String categoryName;

    private long contentCategorySchemaUid;

    private String schemaName;

    public long getContentCategoryUid() {
        return contentCategoryUid;
    }

    public void setContentCategoryUid(long contentCategoryUid) {
        this.contentCategoryUid = contentCategoryUid;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

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

    @Override
    public String toString() {
        return getCategoryName();
    }
}
