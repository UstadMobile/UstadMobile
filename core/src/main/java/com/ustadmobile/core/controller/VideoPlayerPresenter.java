package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.view.ContentEntryDetailView;
import com.ustadmobile.core.view.DummyView;
import com.ustadmobile.core.view.VideoPlayerView;
import com.ustadmobile.lib.db.entities.ContentEntry;

import java.util.Hashtable;

import static com.ustadmobile.core.impl.UstadMobileSystemImpl.ARG_REFERRER;
import static com.ustadmobile.core.view.VideoPlayerView.ARG_AUDIO_PATH;
import static com.ustadmobile.core.view.VideoPlayerView.ARG_CONTENT_ENTRY_ID;
import static com.ustadmobile.core.view.VideoPlayerView.ARG_SRT_PATH;
import static com.ustadmobile.core.view.VideoPlayerView.ARG_VIDEO_PATH;

public class VideoPlayerPresenter extends UstadBaseController<VideoPlayerView> {

    private String videoPath;
    private ContentEntryDao contentEntryDao;
    private String audioPath;
    private String srtPath;
    private String navigation;

    public VideoPlayerPresenter(Object context, Hashtable arguments, VideoPlayerView view) {
        super(context, arguments, view);
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);
        UmAppDatabase appDatabase = UmAccountManager.getRepositoryForActiveAccount(getContext());
        contentEntryDao = appDatabase.getContentEntryDao();


        videoPath = (String) getArguments().get(ARG_VIDEO_PATH);
        audioPath = (String) getArguments().get(ARG_AUDIO_PATH);
        srtPath = (String) getArguments().get(ARG_SRT_PATH);
        navigation = (String) getArguments().get(ARG_REFERRER);
        long entryUuid = Long.parseLong((String) getArguments().get(ARG_CONTENT_ENTRY_ID));
        contentEntryDao.getContentByUuid(entryUuid, new UmCallback<ContentEntry>() {
            @Override
            public void onSuccess(ContentEntry result) {
                view.setVideoInfo(result);
            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });

    }

    public String getVideoPath() {
        return videoPath;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public String getSrtPath() {
        return srtPath;
    }

    public void handleUpNavigation() {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        String lastEntryListArgs = UMFileUtil.getLastReferrerArgsByViewname(ContentEntryDetailView.VIEW_NAME, navigation);
        if (lastEntryListArgs != null) {
            impl.go(ContentEntryDetailView.VIEW_NAME,
                    UMFileUtil.parseURLQueryString(lastEntryListArgs), view.getContext(),
                    UstadMobileSystemImpl.GO_FLAG_CLEAR_TOP | UstadMobileSystemImpl.GO_FLAG_SINGLE_TOP);
        } else {
            impl.go(DummyView.VIEW_NAME,
                    null, view.getContext(),
                    UstadMobileSystemImpl.GO_FLAG_CLEAR_TOP | UstadMobileSystemImpl.GO_FLAG_SINGLE_TOP);
        }

    }
}
