package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.JobStatus;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmObserver;
import com.ustadmobile.core.db.dao.ContainerDao;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryRelatedEntryJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryStatusDao;
import com.ustadmobile.core.db.dao.NetworkNodeDao;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.networkmanager.LocalAvailabilityMonitor;
import com.ustadmobile.core.util.ContentEntryUtil;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.view.ContentEntryDetailView;
import com.ustadmobile.core.view.ContentEntryListView;
import com.ustadmobile.core.view.DummyView;
import com.ustadmobile.lib.db.entities.Container;
import com.ustadmobile.lib.db.entities.ContentEntry;
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

import static com.ustadmobile.core.impl.UstadMobileSystemImpl.ARG_REFERRER;

public class ContentEntryDetailPresenter extends UstadBaseController<ContentEntryDetailView> {

    public static final String ARG_CONTENT_ENTRY_UID = "entryid";

    private String navigation;

    private Long entryUuid;

    private Long containerUid = 0L;

    private LocalAvailabilityMonitor monitor;

    private NetworkNodeDao networkNodeDao;

    private ContainerDao containerDao;

    private AtomicBoolean monitorStatus = new AtomicBoolean(false);

    private UmLiveData<ContentEntryStatus> statusUmLiveData;

    private UmObserver<ContentEntryStatus> statusUmObserver;

    public static final int LOCALLY_AVAILABLE_ICON = 1;

    public static final int LOCALLY_NOT_AVAILABLE_ICON = 2;

    private static final int BAD_NODE_FAILURE_THRESHOLD = 3;

    private static final int TIME_INTERVAL_FROM_LAST_FAILURE = 5;

    private UstadMobileSystemImpl impl;

    public ContentEntryDetailPresenter(Object context, Hashtable arguments,
                                       ContentEntryDetailView viewContract, LocalAvailabilityMonitor monitor) {
        super(context, arguments, viewContract);
        this.monitor = monitor;
        this.impl = UstadMobileSystemImpl.getInstance();

    }

    public void onCreate(Hashtable hashtable) {
        UmAppDatabase repoAppDatabase = UmAccountManager.getRepositoryForActiveAccount(getContext());
        UmAppDatabase appdb = UmAppDatabase.getInstance(getContext());
        ContentEntryRelatedEntryJoinDao contentRelatedEntryDao = repoAppDatabase.getContentEntryRelatedEntryJoinDao();
        ContentEntryDao contentEntryDao = repoAppDatabase.getContentEntryDao();
        ContentEntryStatusDao contentEntryStatusDao = appdb.getContentEntryStatusDao();
        containerDao = repoAppDatabase.getContainerDao();
        networkNodeDao = appdb.getNetworkNodeDao();

        entryUuid = Long.valueOf((String) getArguments().get(ARG_CONTENT_ENTRY_UID));
        navigation = (String) getArguments().get(ARG_REFERRER);

        contentEntryDao.getContentByUuid(entryUuid, new UmCallback<ContentEntry>() {
            @Override
            public void onSuccess(ContentEntry result) {
                String licenseType = getLicenseType(result);
                view.runOnUiThread(() -> {
                    view.setContentEntryLicense(licenseType);
                    view.setContentEntryAuthor(result.getAuthor());
                    view.setContentEntryTitle(result.getTitle());
                    view.setContentEntryDesc(result.getDescription());
                    if (result.getThumbnailUrl() != null
                            && !result.getThumbnailUrl().isEmpty()) {
                        view.loadEntryDetailsThumbnail(result.getThumbnailUrl());
                    }
                });
            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });

        containerDao.findFilesByContentEntryUid(entryUuid, new UmCallback<List<Container>>() {
            @Override
            public void onSuccess(List<Container> result) {
                view.runOnUiThread(() -> {
                    view.setDetailsButtonEnabled(!result.isEmpty());
                    if (!result.isEmpty()) {
                        Container container = result.get(0);
                        view.setDownloadSize(container.getFileSize());
                    }
                });
            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });

        contentRelatedEntryDao.findAllTranslationsForContentEntry(entryUuid,
                new UmCallback<List<ContentEntryRelatedEntryJoinWithLanguage>>() {

                    @Override
                    public void onSuccess(List<ContentEntryRelatedEntryJoinWithLanguage> result) {
                        view.runOnUiThread(() -> {
                            view.setTranslationLabelVisible(!result.isEmpty());
                            view.setFlexBoxVisible(!result.isEmpty());
                            view.setAvailableTranslations(result, entryUuid);
                        });
                    }

                    @Override
                    public void onFailure(Throwable exception) {

                    }
                });

        statusUmLiveData = contentEntryStatusDao.
                findContentEntryStatusByUid(entryUuid);
        statusUmObserver = this::onEntryStatusChanged;
        statusUmLiveData.observe(this, statusUmObserver);
    }

    private String getLicenseType(ContentEntry result) {
        switch (result.getLicenseType()) {
            case ContentEntry.LICENSE_TYPE_CC_BY:
                return "CC BY";
            case ContentEntry.LICENSE_TYPE_CC_BY_SA:
                return "CC BY SA";
            case ContentEntry.LICENSE_TYPE_CC_BY_SA_NC:
                return "CC BY SA NC";
            case ContentEntry.LICENSE_TYPE_CC_BY_NC:
                return "CC BY NC";
            case ContentEntry.LICESNE_TYPE_CC_BY_NC_SA:
                return "CC BY NC SA";
            case ContentEntry.PUBLIC_DOMAIN:
                return "Public Domain";
            case ContentEntry.ALL_RIGHTS_RESERVED:
                return "All Rights Reserved";
        }
        return "";
    }


    private void onEntryStatusChanged(ContentEntryStatus status) {

        boolean isDownloadComplete = status != null &&
                status.getDownloadStatus() == JobStatus.COMPLETE;

        String buttonLabel = impl.getString((status == null || !isDownloadComplete
                ? MessageID.download : MessageID.open), getContext());

        String progressLabel = impl.getString(MessageID.downloading, getContext());

        boolean isDownloading = status != null
                && status.getDownloadStatus() >= JobStatus.RUNNING_MIN
                && status.getDownloadStatus() <= JobStatus.RUNNING_MAX;

        view.runOnUiThread(() -> {
            view.setButtonTextLabel(buttonLabel);
            view.setDownloadButtonVisible(!isDownloading);
            view.setDownloadButtonClickableListener(isDownloadComplete);
            view.setDownloadProgressVisible(isDownloading);
            view.setDownloadProgressLabel(progressLabel);
            view.setLocalAvailabilityStatusViewVisible(isDownloading);
        });

        if (isDownloading) {

            view.runOnUiThread(() -> {
                view.setDownloadButtonVisible(false);
                view.setDownloadProgressVisible(true);
                view.updateDownloadProgress(status.getTotalSize() > 0 ?
                        (float) status.getBytesDownloadSoFar() / (float) status.getTotalSize() : 0f);
            });

        }

        if (!isDownloadComplete) {
            long currentTimeStamp = System.currentTimeMillis();
            long minLastSeen = currentTimeStamp - TimeUnit.MINUTES.toMillis(1);
            long maxFailureFromTimeStamp = currentTimeStamp - TimeUnit.MINUTES.toMillis(
                    TIME_INTERVAL_FROM_LAST_FAILURE);

            new Thread(() -> {

                Container container = containerDao.getMostRecentContainerForContentEntry(getEntryUuid());
                if (container != null) {
                    containerUid = container.getContainerUid();
                    NetworkNode localNetworkNode = networkNodeDao.findLocalActiveNodeByContainerUid(
                            containerUid, minLastSeen, BAD_NODE_FAILURE_THRESHOLD
                            , maxFailureFromTimeStamp);

                    if (localNetworkNode == null && !monitorStatus.get()) {
                        monitorStatus.set(true);
                        monitor.startMonitoringAvailability(this,
                                Collections.singletonList(containerUid));
                    }

                    Set<Long> monitorSet = new HashSet<>();
                    monitorSet.add(localNetworkNode != null ? containerUid : 0L);
                    handleLocalAvailabilityStatus(monitorSet);
                }


            }).start();
        }


    }

    public Long getEntryUuid() {
        return entryUuid;
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

    public void handleDownloadButtonClick(boolean isDownloadComplete, Long entryUuid) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        UmAppDatabase repoAppDatabase = UmAccountManager.getRepositoryForActiveAccount(getContext());
        if (isDownloadComplete) {
            ContentEntryUtil.goToContentEntry(entryUuid,
                    repoAppDatabase, impl,
                    isDownloadComplete,
                    getContext(), new UmCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {

                        }

                        @Override
                        public void onFailure(Throwable exception) {
                            view.runOnUiThread(view::showFileOpenError);
                        }
                    });


        } else {
            Hashtable args = new Hashtable();

            //hard coded strings because these are actually in sharedse
            args.put("contentEntryUid", String.valueOf(this.entryUuid));
            view.runOnUiThread(() -> view.showDownloadOptionsDialog(args));
        }

    }

    public void handleLocalAvailabilityStatus(Set<Long> locallyAvailableEntries) {
        int icon = locallyAvailableEntries.contains(
                containerUid) ? LOCALLY_AVAILABLE_ICON : LOCALLY_NOT_AVAILABLE_ICON;

        String status = impl.getString(
                (icon == LOCALLY_AVAILABLE_ICON ? MessageID.download_locally_availability :
                        MessageID.download_cloud_availability), getContext());

        view.runOnUiThread(() -> view.updateLocalAvailabilityViews(icon, status));
    }


    @Override
    public void onDestroy() {
        if (monitorStatus.get()) {
            monitorStatus.set(false);
            monitor.stopMonitoringAvailability(this);
        }
        statusUmLiveData.removeObserver(statusUmObserver);
        super.onDestroy();
    }

}
