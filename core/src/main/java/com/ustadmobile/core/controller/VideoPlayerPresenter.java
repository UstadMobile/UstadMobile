package com.ustadmobile.core.controller;

import com.ustadmobile.core.view.VideoPlayerView;

import java.util.Hashtable;

import static com.ustadmobile.core.view.VideoPlayerView.ARG_VIDEO_PATH;

public class VideoPlayerPresenter extends UstadBaseController<VideoPlayerView> {

    private String videoPath;

    public VideoPlayerPresenter(Object context, Hashtable arguments, VideoPlayerView view) {
        super(context, arguments, view);
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);
        videoPath = (String) getArguments().get(ARG_VIDEO_PATH);
    }

    public String getVideoPath(){
        return videoPath;
    }
    
    
    
}
