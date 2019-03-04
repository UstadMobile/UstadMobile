package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.JobStatus;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryFileDao;
import com.ustadmobile.core.db.dao.ContentEntryRelatedEntryJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryStatusDao;
import com.ustadmobile.core.db.dao.NetworkNodeDao;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.networkmanager.LocalAvailabilityMonitor;
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
import com.ustadmobile.lib.db.entities.NetworkNode;

import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.ustadmobile.core.controller.ContainerController.ARG_CONTAINERURI;
import static com.ustadmobile.core.impl.UstadMobileSystemImpl.ARG_REFERRER;

public class ContentEntryDetailPresenter extends UstadBaseController<ContentEntryDetailView> {


    public static final String ARG_CONTENT_ENTRY_UID = "entryid";
    private final ContentEntryDetailView viewContract;
    private ContentEntryFileDao contentFileDao;
    private NetworkNodeDao networkNodeDao;
    private String navigation;
    private Long entryUuid;
    private Long contentEntryFileUid = 0L;

    private LocalAvailabilityMonitor monitor;

    private AtomicBoolean monitorStatus = new AtomicBoolean(false);

    public static final int LOCALLY_AVAILABLE_ICON = 1;

    public static final int LOCALLY_NOT_AVAILABLE_ICON = 2;

    private static final int BAD_NODE_FAILURE_THRESHOLD = 3;

    private static final int TIME_INTERVAL_FROM_LAST_FAILURE = 5;

    public ContentEntryDetailPresenter(Object context, Hashtable arguments,
                                       ContentEntryDetailView viewContract, LocalAvailabilityMonitor monitor) {
        super(context, arguments, viewContract);
        this.viewContract = viewContract;
        this.monitor = monitor;

    }

    @Override
    public void onCreate(Hashtable hashtable) {
        UmAppDatabase repoAppDatabase = UmAccountManager.getRepositoryForActiveAccount(getContext());
        UmAppDatabase appdb = UmAppDatabase.getInstance(getContext());
        contentFileDao = repoAppDatabase.getContentEntryFileDao();
        networkNodeDao = appdb.getNetworkNodeDao();
        ContentEntryRelatedEntryJoinDao contentRelatedEntryDao = repoAppDatabase.getContentEntryRelatedEntryJoinDao();
        ContentEntryDao contentEntryDao = repoAppDatabase.getContentEntryDao();
        ContentEntryStatusDao contentEntryStatusDao = appdb.getContentEntryStatusDao();

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

        UmLiveData<ContentEntryStatus> statusUmLiveData = contentEntryStatusDao.findContentEntryStatusByUid(entryUuid);
        statusUmLiveData.observe(this, this::onEntryStatusChanged);
    }


    private void onEntryStatusChanged(ContentEntryStatus status) {
        viewContract.setDownloadProgress(status);

        if(status == null || status.getDownloadStatus() != JobStatus.COMPLETE){

            long currentTimeStamp = System.currentTimeMillis();
            long minLastSeen = currentTimeStamp - TimeUnit.MINUTES.toMillis(1);
            long maxFailureFromTimeStamp = currentTimeStamp - TimeUnit.MINUTES.toMillis(
                    TIME_INTERVAL_FROM_LAST_FAILURE);
            viewContract.setStatusViewsVisible(true);

           new Thread(() -> {
               contentEntryFileUid = contentFileDao.findFilesByContentEntryUid(entryUuid)
                       .get(0).getContentEntryFileUid();
               NetworkNode localNetworkNode = networkNodeDao.findNodeWithContentFileEntry(
                       contentEntryFileUid, minLastSeen,BAD_NODE_FAILURE_THRESHOLD
                       ,maxFailureFromTimeStamp);

               if(localNetworkNode == null && !monitorStatus.get()){
                   monitorStatus.set(true);
                   monitor.startMonitoringAvailability(this,
                           Collections.singletonList(contentEntryFileUid));
               }

               Set<Long> monitorSet = new HashSet<>();
               monitorSet.add(localNetworkNode != null ? contentEntryFileUid : 0L);
               handleUpdateStatusIconAndText(monitorSet);
           }).start();
        }else{
            viewContract.setStatusViewsVisible(false);
        }
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
                            args.put(VideoPlayerView.ARG_VIDEO_PATH, path);
                            args.put(VideoPlayerView.ARG_CONTENT_ENTRY_ID, String.valueOf(entryUuid));
                            impl.go(VideoPlayerView.VIEW_NAME, args, getContext());
                        } else if(result.getMimeType().equals("application/webchunk+zip")){
                            args.put(WebChunkView.ARG_CHUNK_PATH, path);
                            impl.go(WebChunkView.VIEW_NAME, args, getContext());
                        } else if(result.getMimeType().equals("application/epub+zip")){
                            args.put(ARG_CONTAINERURI, path);
                            impl.go(ContainerView.VIEW_NAME, args, getContext());
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

    public void handleUpdateStatusIconAndText(Set<Long> locallyAvailableEntries){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        int icon = locallyAvailableEntries.contains(contentEntryFileUid) ?
                LOCALLY_AVAILABLE_ICON : LOCALLY_NOT_AVAILABLE_ICON;
        String status = impl.getString((icon == LOCALLY_AVAILABLE_ICON
                ? MessageID.download_locally_availability: MessageID.download_cloud_availability)
                ,getContext());
       viewContract.runOnUiThread(() -> viewContract.updateStatusIconAndText(icon,status));
    }

    @Override
    public void onDestroy() {
        if(monitorStatus.get()){
            monitorStatus.set(false);
            monitor.stopMonitoringAvailability(this);
        }
        super.onDestroy();
    }
}
