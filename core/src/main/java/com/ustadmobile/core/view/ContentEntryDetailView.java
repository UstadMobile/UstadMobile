package com.ustadmobile.core.view;

import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryFile;
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoinWithLanguage;
import com.ustadmobile.lib.db.entities.ContentEntryStatus;

import java.util.List;

public interface ContentEntryDetailView extends UstadView {

    String VIEW_NAME = "ContentEntryDetail";

    void setContentInfo(ContentEntry contentEntry);

    void setFileInfo(List<ContentEntryFile> filesByContentEntryUid);

    void setTranslationsAvailable(List<ContentEntryRelatedEntryJoinWithLanguage> result, long entryUuid);

    void setDownloadProgress(ContentEntryStatus status);

    void handleFileOpenError();

    void updateStatusIconAndText(int icon, String status);

}
