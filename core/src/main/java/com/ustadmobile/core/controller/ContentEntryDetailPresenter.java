package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryFileDao;
import com.ustadmobile.core.db.dao.ContentEntryRelatedEntryJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryStatusDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.view.ContainerView;
import com.ustadmobile.core.view.ContentEntryDetailView;
import com.ustadmobile.core.view.ContentEntryListView;
import com.ustadmobile.core.view.DummyView;
import com.ustadmobile.core.view.VideoPlayerView;
import com.ustadmobile.core.view.WebChunkView;
import com.ustadmobile.core.view.XapiPackageView;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryFile;
import com.ustadmobile.lib.db.entities.ContentEntryFileWithStatus;
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoinWithLanguage;
import com.ustadmobile.lib.db.entities.ContentEntryStatus;

import java.util.Hashtable;
import java.util.List;

import static com.ustadmobile.core.controller.ContainerController.ARG_CONTAINERURI;
import static com.ustadmobile.core.impl.UstadMobileSystemImpl.ARG_REFERRER;

public class ContentEntryDetailPresenter extends UstadBaseController<ContentEntryDetailView> {


    public static final String ARG_CONTENT_ENTRY_UID = "entryid";
    private final ContentEntryDetailView viewContract;
    private ContentEntryFileDao contentFileDao;
    private ContentEntryDao contentEntryDao;
    private ContentEntryRelatedEntryJoinDao contentRelatedEntryDao;
    private String navigation;
    private Long entryUuid;
    private ContentEntryStatusDao contentEntryStatusDao;

    private UmLiveData<ContentEntryStatus> statusUmLiveData;

    public ContentEntryDetailPresenter(Object context, Hashtable arguments, ContentEntryDetailView viewContract) {
        super(context, arguments, viewContract);
        this.viewContract = viewContract;

    }

    public void onCreate(Hashtable hashtable) {
        UmAppDatabase repoAppDatabase = UmAccountManager.getRepositoryForActiveAccount(getContext());
        UmAppDatabase appdb = UmAppDatabase.getInstance(getContext());
        contentFileDao = repoAppDatabase.getContentEntryFileDao();
        contentRelatedEntryDao = repoAppDatabase.getContentEntryRelatedEntryJoinDao();
        contentEntryDao = repoAppDatabase.getContentEntryDao();
        contentEntryStatusDao = appdb.getContentEntryStatusDao();

        entryUuid = Long.valueOf((String) getArguments().get(ARG_CONTENT_ENTRY_UID));
        navigation = (String) getArguments().get(ARG_REFERRER);

        contentEntryDao.getContentByUuid(entryUuid, new UmCallback<ContentEntry>() {
            @Override
            public void onSuccess(ContentEntry result) {
                viewContract.setContentInfo(result);
            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });

        contentFileDao.findFilesByContentEntryUid(entryUuid, new UmCallback<List<ContentEntryFile>>() {
            @Override
            public void onSuccess(List<ContentEntryFile> result) {
                viewContract.setFileInfo(result);
            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });

        contentRelatedEntryDao.findAllTranslationsForContentEntry(entryUuid, new UmCallback<List<ContentEntryRelatedEntryJoinWithLanguage>>() {

            @Override
            public void onSuccess(List<ContentEntryRelatedEntryJoinWithLanguage> result) {
                viewContract.setTranslationsAvailable(result, entryUuid);
            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });

        statusUmLiveData = contentEntryStatusDao.findContentEntryStatusByUid(entryUuid);
        statusUmLiveData.observe(this, this::onEntryStatusChanged);
    }

    public void onEntryStatusChanged(ContentEntryStatus status) {
        viewContract.setDownloadProgress(status);
    }


    public void handleClickTranslatedEntry(long uid) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        args.put(ARG_CONTENT_ENTRY_UID, String.valueOf(uid));
        impl.go(ContentEntryDetailView.VIEW_NAME, args, view.getContext());
    }

    public void handleUpNavigation() {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        String lastEntryListArgs = UMFileUtil.getLastReferrerArgsByViewname(ContentEntryListView.VIEW_NAME, navigation);
        if (lastEntryListArgs != null) {
            impl.go(ContentEntryListView.VIEW_NAME,
                    UMFileUtil.parseURLQueryString(lastEntryListArgs), view.getContext(),
                    UstadMobileSystemImpl.GO_FLAG_CLEAR_TOP | UstadMobileSystemImpl.GO_FLAG_SINGLE_TOP);
        } else {
            impl.go(DummyView.VIEW_NAME,
                    null, view.getContext(),
                    UstadMobileSystemImpl.GO_FLAG_CLEAR_TOP | UstadMobileSystemImpl.GO_FLAG_SINGLE_TOP);
        }
    }

    public void handleDownloadButtonClick(boolean isDownloadComplete) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        if (isDownloadComplete) {

            contentFileDao.findLatestCompletedFileForEntry(entryUuid, new UmCallback<ContentEntryFileWithStatus>() {
                @Override
                public void onSuccess(ContentEntryFileWithStatus result) {

                    if (result.getEntryStatus().getFilePath() == null) {
                        viewContract.handleFileOpenError();
                    } else {

                        Hashtable args = new Hashtable();
                        String path = result.getEntryStatus().getFilePath();
                        if (result.getMimeType().equals("application/zip")) {

                            args.put(ARG_CONTAINERURI, path);
                            impl.go(XapiPackageView.VIEW_NAME, args, getContext());
                        } else if (result.getMimeType().equals("video/mp4")) {

                            args.put(VideoPlayerView.ARG_CONTENT_ENTRY_ID, String.valueOf(entryUuid));
                            impl.go(VideoPlayerView.VIEW_NAME, args, getContext());
                        } else if(result.getMimeType().equals("application/webchunk+zip")){

                            args.put(WebChunkView.ARG_CHUNK_PATH, path);
                            impl.go(WebChunkView.VIEW_NAME, args, getContext());
                        } else if(result.getMimeType().equals("application/epub+zip/")){

                            args.put(ARG_CONTAINERURI, path);
                            impl.go(ContainerView.VIEW_NAME, getContext());
                        }

                    }

                }

                @Override
                public void onFailure(Throwable exception) {

                }
            });

        } else {
            Hashtable args = new Hashtable();
            args.put("contentEntryUid", String.valueOf(entryUuid));
            impl.go("DownloadDialog", args, getContext());
        }

    }
}
