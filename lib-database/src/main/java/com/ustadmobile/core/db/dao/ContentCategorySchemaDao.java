package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.ContentCategorySchema;

@UmDao
public abstract class ContentCategorySchemaDao implements BaseDao<ContentCategorySchema> {

    @UmQuery("SELECT * FROM ContentCategorySchema WHERE schemaUrl = :schemaUrl")
    public abstract ContentCategorySchema findBySchemaUrl(String schemaUrl);

    @UmUpdate
    public abstract void updateSchema(ContentCategorySchema schema);
}
