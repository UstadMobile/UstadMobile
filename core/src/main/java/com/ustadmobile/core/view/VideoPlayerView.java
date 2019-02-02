package com.ustadmobile.core.view;

import com.ustadmobile.core.impl.UmCallback;

public interface VideoPlayerView extends UstadView {

    String ARG_VIDEO_PATH = "videopath";

    void loadUrl(String videoPath);
}
