package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

import static com.ustadmobile.lib.db.entities.ContentEntry.TABLE_ID;

@UmEntity(tableId = TABLE_ID)
public class Language {

    public static final int TABLE_ID = 9;

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long langUid;

    private String name;

    // 2 letter code
    private String iso_639_1_standard;

    // 3 letter code
    private String iso_639_2_standard;

    // 3 letter code
    private String iso_639_3_standard;

    @UmSyncLocalChangeSeqNum
    private long langLocalChangeSeqNum;

    @UmSyncMasterChangeSeqNum
    private long langMasterChangeSeqNum;

    public long getLangUid() {
        return langUid;
    }

    public void setLangUid(long langUid) {
        this.langUid = langUid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIso_639_1_standard() {
        return iso_639_1_standard;
    }

    public void setIso_639_1_standard(String iso_639_1_standard) {
        this.iso_639_1_standard = iso_639_1_standard;
    }

    public String getIso_639_2_standard() {
        return iso_639_2_standard;
    }

    public void setIso_639_2_standard(String iso_639_2_standard) {
        this.iso_639_2_standard = iso_639_2_standard;
    }

    public String getIso_639_3_standard() {
        return iso_639_3_standard;
    }

    public void setIso_639_3_standard(String iso_639_3_standard) {
        this.iso_639_3_standard = iso_639_3_standard;
    }

    public long getLangLocalChangeSeqNum() {
        return langLocalChangeSeqNum;
    }

    public void setLangLocalChangeSeqNum(long langLocalChangeSeqNum) {
        this.langLocalChangeSeqNum = langLocalChangeSeqNum;
    }

    public long getLangMasterChangeSeqNum() {
        return langMasterChangeSeqNum;
    }

    public void setLangMasterChangeSeqNum(long langMasterChangeSeqNum) {
        this.langMasterChangeSeqNum = langMasterChangeSeqNum;
    }
}
