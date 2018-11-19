package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.LanguageVariant;
import com.ustadmobile.lib.db.sync.dao.BaseDao;

@UmDao
public abstract class LanguageVariantDao implements BaseDao<LanguageVariant> {

    @UmQuery("SELECT * from LanguageVariant where countryCode = :countryCode")
    public abstract LanguageVariant findByCode(String countryCode);

    @UmUpdate
    public abstract void update(LanguageVariant languageVariant);
}
