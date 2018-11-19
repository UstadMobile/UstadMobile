package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

@UmEntity
public class LanguageVariant {

    @UmPrimaryKey(autoIncrement = true)
    private long langVariantUid;

    private long langUid;

    private String countryCode;

    private String name;

    public long getLangVariantUid() {
        return langVariantUid;
    }

    public void setLangVariantUid(long langVariantUid) {
        this.langVariantUid = langVariantUid;
    }

    public long getLangUid() {
        return langUid;
    }

    public void setLangUid(long langUid) {
        this.langUid = langUid;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
