package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.ContentCategory;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;

@UmDao(selectPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
public abstract class ContentCategoryDao implements SyncableDao<ContentCategory, ContentCategoryDao>  {

    @UmQuery("SELECT * FROM ContentCategory WHERE " +
            "ctnCatContentCategorySchemaUid = :schemaId AND name = :name")
    public abstract ContentCategory findCategoryBySchemaIdAndName(long schemaId, String name);

    @UmUpdate
    public abstract void update(ContentCategory entity);

    @UmQuery("SELECT ContentCategory.* FROM ContentCategory")
    public abstract List<ContentCategory> getPublicContentCategories();
}
