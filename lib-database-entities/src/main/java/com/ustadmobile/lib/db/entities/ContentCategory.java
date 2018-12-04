package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

import static com.ustadmobile.lib.db.entities.ContentCategory.TABLE_ID;


/**
 * Represents a category of content. Each category is tied to a category schema (e.g. category
 *  * "level1" in the schema of "African Storybooks Reading Level"). This allows us to present the user
 *  * with a dropdown list for each different schema.
 */
@UmEntity(tableId = TABLE_ID)
//shortcode = ctnCat
public class ContentCategory {

    public static final int TABLE_ID = 1;

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long contentCategoryUid;

    private long ctnCatContentCategorySchemaUid;

    private String name;

    @UmSyncLocalChangeSeqNum
    private long contentCategoryLocalChangeSeqNum;

    @UmSyncMasterChangeSeqNum
    private long contentCategoryMasterChangeSeqNum;

    public long getContentCategoryUid() {
        return contentCategoryUid;
    }

    public void setContentCategoryUid(long contentCategoryUid) {
        this.contentCategoryUid = contentCategoryUid;
    }

    public long getCtnCatContentCategorySchemaUid() {
        return ctnCatContentCategorySchemaUid;
    }

    public void setCtnCatContentCategorySchemaUid(long ctnCatContentCategorySchemaUid) {
        this.ctnCatContentCategorySchemaUid = ctnCatContentCategorySchemaUid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getContentCategoryLocalChangeSeqNum() {
        return contentCategoryLocalChangeSeqNum;
    }

    public void setContentCategoryLocalChangeSeqNum(long contentCategoryLocalChangeSeqNum) {
        this.contentCategoryLocalChangeSeqNum = contentCategoryLocalChangeSeqNum;
    }

    public long getContentCategoryMasterChangeSeqNum() {
        return contentCategoryMasterChangeSeqNum;
    }

    public void setContentCategoryMasterChangeSeqNum(long contentCategoryMasterChangeSeqNum) {
        this.contentCategoryMasterChangeSeqNum = contentCategoryMasterChangeSeqNum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ContentCategory category = (ContentCategory) o;

        if (contentCategoryUid != category.contentCategoryUid) return false;
        if (ctnCatContentCategorySchemaUid != category.ctnCatContentCategorySchemaUid) return false;
        return name != null ? name.equals(category.name) : category.name == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (contentCategoryUid ^ (contentCategoryUid >>> 32));
        result = 31 * result + (int) (ctnCatContentCategorySchemaUid ^ (ctnCatContentCategorySchemaUid >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
