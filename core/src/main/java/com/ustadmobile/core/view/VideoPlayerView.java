package com.ustadmobile.core.view;

import com.ustadmobile.lib.db.entities.ContentEntry;

public interface VideoPlayerView extends UstadView {

    String ARG_VIDEO_PATH = "videopath";

    String ARG_AUDIO_PATH = "audiopath";

    String ARG_SRT_PATH = "srtpath";

    String ARG_CONTENT_ENTRY_ID = "entryid";

    String VIEW_NAME = "VideoPlayer";

    void loadUrl(String videoPath);

    void setVideoInfo(ContentEntry result);
}
