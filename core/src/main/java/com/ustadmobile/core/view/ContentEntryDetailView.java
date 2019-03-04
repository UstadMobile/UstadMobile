package com.ustadmobile.core.view;

import com.ustadmobile.lib.db.entities.Container;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoinWithLanguage;

import java.util.List;

public interface ContentEntryDetailView extends UstadView {

    String VIEW_NAME = "ContentEntryDetail";

    void setContentInfo(ContentEntry contentEntry, String licenseType);

    void setFileInfo(List<Container> filesByContentEntryUid);

    void setTranslationsAvailable(List<ContentEntryRelatedEntryJoinWithLanguage> result, long entryUuid);

    void showProgress(float progressValue);

    void showButton(boolean isDownloaded);

    void handleFileOpenError();

    void updateStatusIconAndText(int icon, String status);

    void setStatusViewsVisible(boolean visible);

}
