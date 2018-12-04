package com.ustadmobile.lib.db.entities;

public class ContentEntryRelatedEntryJoinWithLanguage {

    private long cerejContentEntryUid;

    private long cerejRelatedEntryUid;

    private String languageName;

    public String getLanguageName() {
        return languageName;
    }

    public void setLanguageName(String languageName) {
        this.languageName = languageName;
    }

    public long getCerejContentEntryUid() {
        return cerejContentEntryUid;
    }


    public void setCerejContentEntryUid(long cerejContentEntryUid) {
        this.cerejContentEntryUid = cerejContentEntryUid;
    }


    public long getCerejRelatedEntryUid() {
        return cerejRelatedEntryUid;
    }

    public void setCerejRelatedEntryUid(long cerejRelatedEntryUid) {
        this.cerejRelatedEntryUid = cerejRelatedEntryUid;
    }
}
