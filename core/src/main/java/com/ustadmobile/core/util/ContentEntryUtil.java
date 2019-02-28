package com.ustadmobile.core.util;

import com.ustadmobile.core.controller.ContentEntryDetailPresenter;
import com.ustadmobile.core.db.JobStatus;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UmCallbackUtil;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.ContainerView;
import com.ustadmobile.core.view.ContentEntryDetailView;
import com.ustadmobile.core.view.VideoPlayerView;
import com.ustadmobile.core.view.WebChunkView;
import com.ustadmobile.core.view.XapiPackageView;
import com.ustadmobile.lib.db.entities.ContentEntryFileWithStatus;
import com.ustadmobile.lib.db.entities.ContentEntryWithContentEntryStatus;

import java.util.Hashtable;

import static com.ustadmobile.core.controller.ContainerController.ARG_CONTAINERURI;

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

            dbRepo.getContentEntryFileDao().findLatestCompletedFileForEntry(entryStatus.getContentEntryUid(), new UmCallback<ContentEntryFileWithStatus>() {
                @Override
                public void onSuccess(ContentEntryFileWithStatus result) {


                    if (result.getEntryStatus().getFilePath() == null) {
                        UmCallbackUtil.onFailIfNotNull(callback, new IllegalArgumentException(
                                "No file found for " + entryStatus.getContentEntryStatus()));
                    } else {

                        Hashtable args = new Hashtable();
                        String path = result.getEntryStatus().getFilePath();
                        switch (result.getMimeType()) {
                            case "application/zip":

                                args.put(ARG_CONTAINERURI, path);
                                impl.go(XapiPackageView.VIEW_NAME, args, context);
                                break;
                            case "video/mp4":

                                args.put(VideoPlayerView.ARG_VIDEO_PATH, path);
                                args.put(VideoPlayerView.ARG_CONTENT_ENTRY_ID, String.valueOf(entryStatus.getContentEntryUid()));
                                impl.go(VideoPlayerView.VIEW_NAME, args, context);
                                break;
                            case "application/webchunk+zip":

                                args.put(WebChunkView.ARG_CHUNK_PATH, path);
                                args.put(WebChunkView.ARG_CONTENT_ENTRY_ID, String.valueOf(entryStatus.getContentEntryUid()));
                                impl.go(WebChunkView.VIEW_NAME, args, context);
                                break;
                            case "application/epub+zip":

                                args.put(ARG_CONTAINERURI, path);
                                impl.go(ContainerView.VIEW_NAME, args, context);
                                break;
                            case "application/khan-video+zip":

                                // TODO unzip and give the path to both
                                args.put(VideoPlayerView.ARG_VIDEO_PATH, path);
                                args.put(VideoPlayerView.ARG_AUDIO_PATH, path);
                                args.put(VideoPlayerView.ARG_SRT_PATH, path);
                                args.put(VideoPlayerView.ARG_CONTENT_ENTRY_ID, String.valueOf(entryStatus.getContentEntryUid()));
                                impl.go(VideoPlayerView.VIEW_NAME, args, context);
                                break;
                        }

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
