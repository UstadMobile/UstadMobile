package com.ustadmobile.core.view;

import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryFile;

import java.util.List;

public interface ContentEntryDetailView extends UstadView {

    public static final String VIEW_NAME = "ContentEntryDetail";

    void setContentInfo(ContentEntry contentEntry);

    void setFileInfo(List<ContentEntryFile> filesByContentEntryUid);

    void setLanguageContent(List<ContentEntry> result);
}
