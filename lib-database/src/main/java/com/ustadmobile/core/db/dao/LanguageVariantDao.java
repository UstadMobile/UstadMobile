package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.LanguageVariant;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;

@UmDao(selectPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
public abstract class LanguageVariantDao implements SyncableDao<LanguageVariant, LanguageVariantDao> {

    @UmQuery("SELECT * FROM LanguageVariant WHERE countryCode = :countryCode LIMIT 1")
    public abstract LanguageVariant findByCode(String countryCode);

    @UmUpdate
    public abstract void update(LanguageVariant languageVariant);

    @UmQuery("SELECT * FROM LanguageVariant")
    public abstract List<LanguageVariant> getPublicLanguageVariants();
}
