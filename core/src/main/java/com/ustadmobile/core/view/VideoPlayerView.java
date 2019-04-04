package com.ustadmobile.core.view;

import com.ustadmobile.lib.db.entities.ContentEntry;

public interface VideoPlayerView extends UstadView {

    String ARG_CONTENT_ENTRY_ID = "entryid";

    String ARG_CONTAINER_UID = "containerUid";

    String VIEW_NAME = "VideoPlayer";

    void loadUrl(String videoPath);

    void setVideoInfo(ContentEntry result);

    void setVideoParams(String videoPath, String audioPath, String srtPath);
}
