package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

/**
 * Represents a category of content. Each category is tied to a category schema (e.g. category
 * "level1" in the schema of "African Storybooks Reading Level"). This allows us to present the user
 * with a dropdown list for each different schema.
 */
@UmEntity
//shortcode = ctnCat
public class ContentCategory {

    @UmPrimaryKey
    private int contentCategoryUid;

    private int ctnCatContentCategorySchemaUid;

    private String name;

    public int getContentCategoryUid() {
        return contentCategoryUid;
    }

    public void setContentCategoryUid(int contentCategoryUid) {
        this.contentCategoryUid = contentCategoryUid;
    }

    public int getCtnCatContentCategorySchemaUid() {
        return ctnCatContentCategorySchemaUid;
    }

    public void setCtnCatContentCategorySchemaUid(int ctnCatContentCategorySchemaUid) {
        this.ctnCatContentCategorySchemaUid = ctnCatContentCategorySchemaUid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
