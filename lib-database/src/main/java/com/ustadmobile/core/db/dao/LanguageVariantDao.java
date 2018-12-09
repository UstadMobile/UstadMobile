package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.LanguageVariant;
import com.ustadmobile.lib.db.sync.dao.BaseDao;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

@UmDao(readPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
public abstract class LanguageVariantDao implements SyncableDao<LanguageVariant, LanguageVariantDao> {

    @UmQuery("SELECT * from LanguageVariant where countryCode = :countryCode")
    public abstract LanguageVariant findByCode(String countryCode);

    @UmUpdate
    public abstract void update(LanguageVariant languageVariant);
}
