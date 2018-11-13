package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

/**
 * Represents a category of content. Each category is tied to a category schema (e.g. category
 *  * "level1" in the schema of "African Storybooks Reading Level"). This allows us to present the user
 *  * with a dropdown list for each different schema.
 */
@UmEntity
//shortcode = ctnCat
public class ContentCategory {

    @UmPrimaryKey(autoIncrement = true)
    private long contentCategoryUid;

    private long ctnCatContentCategorySchemaUid;

    private String name;

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
}
