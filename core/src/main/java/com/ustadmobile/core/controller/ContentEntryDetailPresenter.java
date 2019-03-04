package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.JobStatus;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.dao.ContainerDao;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryRelatedEntryJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryStatusDao;
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

import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.ustadmobile.core.impl.UstadMobileSystemImpl.ARG_REFERRER;

public class ContentEntryDetailPresenter extends UstadBaseController<ContentEntryDetailView> {

    //TODO: Handle monitoring availability using container

    public static final String ARG_CONTENT_ENTRY_UID = "entryid";
    private final ContentEntryDetailView viewContract;
    private ContentEntryDao contentEntryDao;
    private ContentEntryRelatedEntryJoinDao contentRelatedEntryDao;
    private String navigation;
    private Long entryUuid;
    private ContentEntryStatusDao contentEntryStatusDao;

    private UmLiveData<ContentEntryStatus> statusUmLiveData;

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

    public void onCreate(Hashtable hashtable) {
        UmAppDatabase repoAppDatabase = UmAccountManager.getRepositoryForActiveAccount(getContext());
        UmAppDatabase appdb = UmAppDatabase.getInstance(getContext());
        contentRelatedEntryDao = repoAppDatabase.getContentEntryRelatedEntryJoinDao();
        contentEntryDao = repoAppDatabase.getContentEntryDao();
        contentEntryStatusDao = appdb.getContentEntryStatusDao();
        ContainerDao containerDao = repoAppDatabase.getContainerDao();

        entryUuid = Long.valueOf((String) getArguments().get(ARG_CONTENT_ENTRY_UID));
        navigation = (String) getArguments().get(ARG_REFERRER);

        contentEntryDao.getContentByUuid(entryUuid, new UmCallback<ContentEntry>() {
            @Override
            public void onSuccess(ContentEntry result) {
                String licenseType = getLicenseType(result);
                viewContract.setContentInfo(result, licenseType);
            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });

        containerDao.findFilesByContentEntryUid(entryUuid, new UmCallback<List<Container>>() {
            @Override
            public void onSuccess(List<Container> result) {
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

    private String getLicenseType(ContentEntry result) {
        switch (result.getLicenseType()){
            case ContentEntry.LICENSE_TYPE_CC_BY:
                return "CC BY";
            case ContentEntry.LICENSE_TYPE_CC_BY_SA:
                return "CC BY SA";
            case ContentEntry.LICESNE_TYPE_CC_BY_NC_SA:
                return "CC BY NC SA";
            case ContentEntry.LICENSE_TYPE_CC_BY_SA_NC:
                return "CC BY SA NC";
            case ContentEntry.PUBLIC_DOMAIN:
                return "Public Domain";
            case ContentEntry.ALL_RIGHTS_RESERVED:
                return "All Rights Reserved";
        }
        return "";
    }

    public void onEntryStatusChanged(ContentEntryStatus status) {
        if (status != null) {
            if (status.getDownloadStatus() == 0 || status.getDownloadStatus() == JobStatus.COMPLETE) {
                viewContract.showButton(status.getDownloadStatus() == JobStatus.COMPLETE);
            } else {
                viewContract.showProgress(status.getTotalSize() > 0 ? (float) status.getBytesDownloadSoFar() /
                        (float) status.getTotalSize() : 0);
            }
        } else {
            viewContract.showButton(false);
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
                            viewContract.handleFileOpenError();
                        }
                    });


        } else {
            Hashtable args = new Hashtable();

            //hard coded strings because these are actually in sharedse
            args.put("contentEntryUid", String.valueOf(this.entryUuid));
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
