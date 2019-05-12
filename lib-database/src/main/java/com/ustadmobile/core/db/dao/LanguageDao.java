package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.Language;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;

@UmDao(selectPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
public abstract class LanguageDao implements SyncableDao<Language, LanguageDao> {


    @UmQuery("SELECT * FROM Language WHERE name = :name LIMIT 1")
    public abstract Language findByName(String name);

    @UmQuery("SELECT * FROM Language WHERE iso_639_1_standard = :langCode LIMIT 1")
    public abstract Language findByTwoCode(String langCode);

    @UmQuery("SELECT COUNT(*) FROM LANGUAGE")
    public abstract int totalLanguageCount();

    @UmUpdate
    public abstract void update(Language entity);

    @UmQuery("SELECT * FROM Language")
    public abstract List<Language> getPublicLanguages();
}
