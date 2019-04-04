package com.ustadmobile.core.view;

import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoinWithLanguage;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

public interface ContentEntryDetailView extends UstadView, ViewWithErrorNotifier {

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

    void showFileOpenError(String message, int actionMessageId, String mimeType);

    void showFileOpenError(String message);

    void updateLocalAvailabilityViews(int icon, String status);

    void setLocalAvailabilityStatusViewVisible(boolean visible);

    void setTranslationLabelVisible(boolean visible);

    void setFlexBoxVisible(boolean visible);

    void setDownloadProgressVisible(boolean visible);

    void setDownloadProgressLabel(String progressLabel);

    void setDownloadButtonClickableListener(boolean isDownloadComplete);

    void showDownloadOptionsDialog(HashMap<String,String> hashtable);

    Set<Long> getAllKnowAvailabilityStatus();

}
