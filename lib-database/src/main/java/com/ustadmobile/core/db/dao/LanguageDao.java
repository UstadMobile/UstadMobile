package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.Language;

import java.util.List;

@UmDao
public abstract class LanguageDao implements BaseDao<Language> {


    @UmQuery("SELECT * from Language WHERE name = :name")
    public abstract Language findByName(String name);

    @UmUpdate
    public abstract void updateLanguage(Language language);

    @UmQuery("SELECT * from Language WHERE iso_639_1_standard = :langCode")
    public abstract Language findByTwoCode(String langCode);

    @UmQuery("Select COUNT(*) FROM LANGUAGE")
    public abstract int totalLanguageCount();
}
