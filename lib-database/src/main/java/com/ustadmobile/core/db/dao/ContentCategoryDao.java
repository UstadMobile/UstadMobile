package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.ContentCategory;

@UmDao
public abstract class ContentCategoryDao implements BaseDao<ContentCategory> {

    @UmQuery("SELECT * from ContentCategory WHERE " +
            "ctnCatContentCategorySchemaUid = :schemaId AND name = :name")
    public abstract ContentCategory findCategoryBySchemaIdAndName(long schemaId, String name);

    @UmUpdate
    public abstract void updateCategory(ContentCategory category);
}
