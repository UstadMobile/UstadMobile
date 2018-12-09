package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

import static com.ustadmobile.lib.db.entities.LanguageVariant.TABLE_ID;


@UmEntity(tableId = TABLE_ID)
public class LanguageVariant {

    public static final int TABLE_ID = 10;

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long langVariantUid;

    private long langUid;

    private String countryCode;

    private String name;

    @UmSyncLocalChangeSeqNum
    private long langVariantLocalChangeSeqNum;

    @UmSyncMasterChangeSeqNum
    private long langVariantMasterChangeSeqNum;


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

    public long getLangVariantLocalChangeSeqNum() {
        return langVariantLocalChangeSeqNum;
    }

    public void setLangVariantLocalChangeSeqNum(long langVariantLocalChangeSeqNum) {
        this.langVariantLocalChangeSeqNum = langVariantLocalChangeSeqNum;
    }

    public long getLangVariantMasterChangeSeqNum() {
        return langVariantMasterChangeSeqNum;
    }

    public void setLangVariantMasterChangeSeqNum(long langVariantMasterChangeSeqNum) {
        this.langVariantMasterChangeSeqNum = langVariantMasterChangeSeqNum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LanguageVariant that = (LanguageVariant) o;

        if (langVariantUid != that.langVariantUid) return false;
        if (langUid != that.langUid) return false;
        if (countryCode != null ? !countryCode.equals(that.countryCode) : that.countryCode != null)
            return false;
        return name != null ? name.equals(that.name) : that.name == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (langVariantUid ^ (langVariantUid >>> 32));
        result = 31 * result + (int) (langUid ^ (langUid >>> 32));
        result = 31 * result + (countryCode != null ? countryCode.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
