package com.ustadmobile.core.view;

import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoinWithLanguage;

import java.util.Hashtable;
import java.util.List;

public interface ContentEntryDetailView extends UstadView {

    String VIEW_NAME = "ContentEntryDetail";


    void setContentEntryTitle(String title);

    void setContentEntryDesc(String desc);

    void setContentEntryLicense(String license);

    void setContentEntryAuthor(String author);

    void setDetailsButtonEnabled(boolean enabled);

    void setDownloadSize(long fileSize);

    void loadEntryDetailsThumbnail(String thumbnailUrl);

    void setAvailableTranslations(List<ContentEntryRelatedEntryJoinWithLanguage> result, long entryUuid);

    void updateDownloadProgress(float progressValue);

    void setDownloadButtonVisible(boolean visible);

    void setButtonTextLabel(String textLabel);

    void showFileOpenError();

    void updateLocalAvailabilityViews(int icon, String status);

    void setLocalAvailabilityStatusViewVisible(boolean visible);

    void setTranslationLabelVisible(boolean visible);

    void setFlexBoxVisible(boolean visible);

    void setDownloadProgressVisible(boolean visible);

    void setDownloadProgressLabel(String progressLabel);

    void setDownloadButtonClickableListener(boolean isDownloadComplete);

    void showDownloadOptionsDialog(Hashtable hashtable);

}
