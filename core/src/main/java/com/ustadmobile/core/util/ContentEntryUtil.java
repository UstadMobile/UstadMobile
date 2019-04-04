package com.ustadmobile.core.util;

import com.ustadmobile.core.controller.ContentEntryDetailPresenter;
import com.ustadmobile.core.db.JobStatus;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UmCallbackUtil;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.ContentEntryDetailView;
import com.ustadmobile.core.view.EpubContentView;
import com.ustadmobile.core.view.VideoPlayerView;
import com.ustadmobile.core.view.WebChunkView;
import com.ustadmobile.core.view.XapiPackageContentView;
import com.ustadmobile.lib.db.entities.Container;
import com.ustadmobile.lib.db.entities.ContainerEntryWithContainerEntryFile;
import com.ustadmobile.lib.db.entities.ContentEntryWithContentEntryStatus;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

public class ContentEntryUtil {

    public static final HashMap<String, String> mimeTypeToPlayStoreIdMap = new HashMap<>();

    static {
        mimeTypeToPlayStoreIdMap.put("text/plain", "com.microsoft.office.word");
        mimeTypeToPlayStoreIdMap.put("audio/mpeg", "music.musicplayer");
        mimeTypeToPlayStoreIdMap.put("application/pdf", "com.adobe.reader");
        mimeTypeToPlayStoreIdMap.put("application/vnd.openxmlformats-officedocument.presentationml.presentation", "com.microsoft.office.powerpoint");
        mimeTypeToPlayStoreIdMap.put("com.microsoft.office.powerpoint", "com.microsoft.office.powerpoint");
        mimeTypeToPlayStoreIdMap.put("image/jpeg", "com.pcvirt.ImageViewer");
        mimeTypeToPlayStoreIdMap.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "com.microsoft.office.word");
    }


    public static void goToContentEntry(long contentEntryUid, UmAppDatabase dbRepo,
                                        UstadMobileSystemImpl impl, boolean openEntryIfNotDownloaded,
                                        Object context,
                                        UmCallback<Void> callback) {

        dbRepo.getContentEntryDao().findByUidWithContentEntryStatus(contentEntryUid, new UmCallback<ContentEntryWithContentEntryStatus>() {

            @Override
            public void onSuccess(ContentEntryWithContentEntryStatus result) {
                if (result != null) {
                    goToViewIfDownloaded(result, dbRepo, impl, openEntryIfNotDownloaded, context, callback);
                }
            }

            @Override
            public void onFailure(Throwable exception) {
                UmCallbackUtil.onFailIfNotNull(callback, exception);
            }
        });


    }

    private static void goToViewIfDownloaded(ContentEntryWithContentEntryStatus entryStatus,
                                             UmAppDatabase dbRepo,
                                             UstadMobileSystemImpl impl, boolean openEntryIfNotDownloaded,
                                             Object context,
                                             UmCallback<Void> callback) {

        if (entryStatus.getContentEntryStatus() != null
                && entryStatus.getContentEntryStatus().getDownloadStatus() == JobStatus.COMPLETE) {

            dbRepo.getContainerDao().getMostRecentContainerForContentEntryAsync(entryStatus.getContentEntryUid(), new UmCallback<Container>() {
                @Override
                public void onSuccess(Container result) {
                    HashMap<String , String> args = new HashMap<>();
                    String viewName = null;
                    switch (result.getMimeType()) {
                        case "application/zip":
                        case "application/tincan+zip":
                            args.put(XapiPackageContentView.ARG_CONTAINER_UID, String.valueOf(result.getContainerUid()));
                            viewName = XapiPackageContentView.VIEW_NAME;
                            break;
                        case "video/mp4":

                            args.put(VideoPlayerView.ARG_CONTAINER_UID, String.valueOf(result.getContainerUid()));
                            args.put(VideoPlayerView.ARG_CONTENT_ENTRY_ID, String.valueOf(result.getContainerContentEntryUid()));
                            viewName = VideoPlayerView.VIEW_NAME;
                            break;
                        case "application/webchunk+zip":

                            args.put(WebChunkView.ARG_CONTAINER_UID, String.valueOf(result.getContainerUid()));
                            args.put(WebChunkView.ARG_CONTENT_ENTRY_ID, String.valueOf(result.getContainerContentEntryUid()));
                            viewName = WebChunkView.VIEW_NAME;
                            break;
                        case "application/epub+zip":

                            args.put(EpubContentView.ARG_CONTAINER_UID, String.valueOf(result.getContainerUid()));
                            viewName = EpubContentView.VIEW_NAME;
                            break;
                        case "application/khan-video+zip":

                            args.put(VideoPlayerView.ARG_CONTAINER_UID, String.valueOf(result.getContainerUid()));
                            args.put(VideoPlayerView.ARG_CONTENT_ENTRY_ID, String.valueOf(result.getContainerContentEntryUid()));
                            viewName = VideoPlayerView.VIEW_NAME;
                            break;
                        default:
                            dbRepo.getContainerEntryDao().findByContainer(result.getContainerUid(), new UmCallback<List<ContainerEntryWithContainerEntryFile>>() {
                                @Override
                                public void onSuccess(List<ContainerEntryWithContainerEntryFile> resultList) {
                                    if (resultList.isEmpty()) {
                                        UmCallbackUtil.onFailIfNotNull(callback, new IllegalArgumentException("No file found"));
                                        return;
                                    }

                                    ContainerEntryWithContainerEntryFile containerEntryWithContainerEntryFile = resultList.get(0);
                                    if (containerEntryWithContainerEntryFile != null) {
                                        impl.openFileInDefaultViewer(context, containerEntryWithContainerEntryFile.
                                                        getContainerEntryFile().getCefPath(),
                                                result.getMimeType(), callback);
                                    }

                                }

                                @Override
                                public void onFailure(Throwable exception) {
                                    UmCallbackUtil.onFailIfNotNull(callback, exception);
                                }
                            });

                    }
                    if (viewName != null) {
                        impl.go(viewName, args, context);
                        UmCallbackUtil.onSuccessIfNotNull(callback, null);
                    }
                }

                @Override
                public void onFailure(Throwable exception) {
                    UmCallbackUtil.onFailIfNotNull(callback, exception);
                }
            });

        } else if (openEntryIfNotDownloaded) {
            HashMap<String, String> args = new HashMap<>();
            args.put(ContentEntryDetailPresenter.ARG_CONTENT_ENTRY_UID,
                    String.valueOf(entryStatus.getContentEntryUid()));
            impl.go(ContentEntryDetailView.VIEW_NAME, args, context);
            UmCallbackUtil.onSuccessIfNotNull(callback, null);
        }

    }

    public static void goToContentEntryBySourceUrl(String sourceUrl, UmAppDatabase dbRepo,
                                                   UstadMobileSystemImpl impl, boolean openEntryIfNotDownloaded,
                                                   Object context,
                                                   UmCallback<Void> callback) {

        dbRepo.getContentEntryDao().findBySourceUrlWithContentEntryStatus(sourceUrl, new UmCallback<ContentEntryWithContentEntryStatus>() {
            @Override
            public void onSuccess(ContentEntryWithContentEntryStatus result) {
                if (result != null) {
                    goToViewIfDownloaded(result, dbRepo, impl, openEntryIfNotDownloaded, context, callback);
                } else {
                    UmCallbackUtil.onFailIfNotNull(callback, new IllegalArgumentException("No such content entry"));
                }
            }

            @Override
            public void onFailure(Throwable exception) {
                UmCallbackUtil.onFailIfNotNull(callback, exception);
            }
        });


    }

    /**
     * Used to handle navigating when the user clicks a link in content.
     *
     * @param viewDestination
     * @param dbRepo
     * @param impl
     * @param openEntryIfNotDownloaded
     * @param context
     * @param callback
     */
    public static void goToContentEntryByViewDestination(String viewDestination, UmAppDatabase dbRepo,
                                                         UstadMobileSystemImpl impl, boolean openEntryIfNotDownloaded,
                                                         Object context, UmCallback<Void> callback) {
        //substitute for previously scraped content
        viewDestination = viewDestination.replace("content-detail?",
                ContentEntryDetailView.VIEW_NAME + "?");

        HashMap<String , String> params = UMFileUtil.parseURLQueryString(viewDestination);
        if (params.containsKey("sourceUrl")) {
            goToContentEntryBySourceUrl(params.get("sourceUrl"), dbRepo,
                    impl, openEntryIfNotDownloaded, context,
                    callback);
        }

    }


}
