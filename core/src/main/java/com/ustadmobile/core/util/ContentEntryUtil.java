package com.ustadmobile.core.util;

import com.ustadmobile.core.controller.ContentEntryDetailPresenter;
import com.ustadmobile.core.db.JobStatus;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UmCallbackUtil;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.EpubContentView;
import com.ustadmobile.core.view.ContentEntryDetailView;
import com.ustadmobile.core.view.VideoPlayerView;
import com.ustadmobile.core.view.WebChunkView;
import com.ustadmobile.core.view.XapiPackageView;
import com.ustadmobile.lib.db.entities.Container;
import com.ustadmobile.lib.db.entities.ContentEntryWithContentEntryStatus;

import java.util.Hashtable;

import static com.ustadmobile.core.controller.EpubContentPresenter.ARG_CONTAINERURI;

public class ContentEntryUtil {

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
                    Hashtable args = new Hashtable();
                    switch (result.getMimeType()) {
                        case "application/zip":

                            args.put(ARG_CONTAINERURI, String.valueOf(result.getContainerUid()));
                            impl.go(XapiPackageView.VIEW_NAME, args, context);
                            break;
                        case "video/mp4":

                            args.put(VideoPlayerView.ARG_CONTAINER_UID, String.valueOf(result.getContainerUid()));
                            args.put(VideoPlayerView.ARG_CONTENT_ENTRY_ID, String.valueOf(result.getContainerContentEntryUid()));
                            impl.go(VideoPlayerView.VIEW_NAME, args, context);
                            break;
                        case "application/webchunk+zip":

                            args.put(WebChunkView.ARG_CONTAINER_UID, String.valueOf(result.getContainerUid()));
                            args.put(WebChunkView.ARG_CONTENT_ENTRY_ID, String.valueOf(result.getContainerContentEntryUid()));
                            impl.go(WebChunkView.VIEW_NAME, args, context);
                            break;
                        case "application/epub+zip":

                            args.put(EpubContentView.ARG_CONTAINER_UID, String.valueOf(result.getContainerUid()));
                            impl.go(EpubContentView.VIEW_NAME, args, context);
                            break;
                        case "application/khan-video+zip":

                            args.put(VideoPlayerView.ARG_CONTAINER_UID, String.valueOf(result.getContainerUid()));
                            args.put(VideoPlayerView.ARG_CONTENT_ENTRY_ID, String.valueOf(result.getContainerContentEntryUid()));
                            impl.go(VideoPlayerView.VIEW_NAME, args, context);
                            break;

                    }
                }

                @Override
                public void onFailure(Throwable exception) {
                    UmCallbackUtil.onFailIfNotNull(callback, exception);
                }
            });

        } else if (openEntryIfNotDownloaded) {
            Hashtable<String, String> args = new Hashtable<>();
            args.put(ContentEntryDetailPresenter.ARG_CONTENT_ENTRY_UID,
                    String.valueOf(entryStatus.getContentEntryUid()));
            impl.go(ContentEntryDetailView.VIEW_NAME, args, context);
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

        Hashtable params = UMFileUtil.parseURLQueryString(viewDestination);
        if (params.containsKey("sourceUrl")) {
            goToContentEntryBySourceUrl(params.get("sourceUrl").toString(), dbRepo,
                    impl, openEntryIfNotDownloaded, context,
                    callback);
        }

    }


}
