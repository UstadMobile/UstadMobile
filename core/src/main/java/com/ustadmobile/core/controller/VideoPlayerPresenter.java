package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContainerEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.view.ContentEntryDetailView;
import com.ustadmobile.core.view.DummyView;
import com.ustadmobile.core.view.VideoPlayerView;
import com.ustadmobile.lib.db.entities.ContainerEntryWithContainerEntryFile;
import com.ustadmobile.lib.db.entities.ContentEntry;

import java.util.Hashtable;
import java.util.List;

import static com.ustadmobile.core.impl.UstadMobileSystemImpl.ARG_REFERRER;
import static com.ustadmobile.core.view.VideoPlayerView.ARG_CONTAINER_UID;
import static com.ustadmobile.core.view.VideoPlayerView.ARG_CONTENT_ENTRY_ID;

public class VideoPlayerPresenter extends UstadBaseController<VideoPlayerView> {

    private ContentEntryDao contentEntryDao;
    private String navigation;
    private String audioPath;
    private String srtPath;
    private String videoPath;

    public VideoPlayerPresenter(Object context, Hashtable arguments, VideoPlayerView view) {
        super(context, arguments, view);
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);
        UmAppDatabase db = UmAppDatabase.getInstance(getContext());
        UmAppDatabase dbRepo = UmAccountManager.getRepositoryForActiveAccount(getContext());
        contentEntryDao = dbRepo.getContentEntryDao();
        ContainerEntryDao containerEntryDao = db.getContainerEntryDao();

        navigation = (String) getArguments().get(ARG_REFERRER);
        long entryUuid = Long.parseLong((String) getArguments().get(ARG_CONTENT_ENTRY_ID));
        long containerUid = Long.parseLong((String) getArguments().get(ARG_CONTAINER_UID));

        contentEntryDao.getContentByUuid(entryUuid, new UmCallback<ContentEntry>() {
            @Override
            public void onSuccess(ContentEntry result) {
                view.setVideoInfo(result);
            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });


        containerEntryDao.findByContainer(containerUid, new UmCallback<List<ContainerEntryWithContainerEntryFile>>() {
            @Override
            public void onSuccess(List<ContainerEntryWithContainerEntryFile> result) {

                for (ContainerEntryWithContainerEntryFile entry : result) {

                    String fileInContainer = entry.getCePath();
                    if (fileInContainer.endsWith(".mp4") || fileInContainer.endsWith(".webm")) {
                        videoPath = entry.getContainerEntryFile().getCefPath();
                    } else if (fileInContainer.equals("audio.c2")) {
                        audioPath = entry.getContainerEntryFile().getCefPath();
                    } else if (fileInContainer.equals("subtitle.srt")) {
                        srtPath = entry.getContainerEntryFile().getCefPath();
                    }
                }

                view.runOnUiThread(() -> {
                    view.setVideoParams(videoPath, audioPath, srtPath);
                });
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
