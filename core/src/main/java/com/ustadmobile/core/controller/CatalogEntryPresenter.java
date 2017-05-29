package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.AcquisitionManager;
import com.ustadmobile.core.impl.AcquisitionStatusEvent;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.opds.UstadJSOPDSItem;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.view.CatalogEntryView;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Created by mike on 4/17/17.
 */

public class CatalogEntryPresenter extends BaseCatalogController{

    private CatalogEntryView catalogEntryView;

    private Hashtable args;

    public static final String ARG_ENTRY_OPDS_STR = "opds_str";

    private UstadJSOPDSEntry entry;

    private UstadJSOPDSFeed entryFeed;

    public CatalogEntryPresenter(Object context) {
        super(context);
    }

    public CatalogEntryPresenter(Object context, CatalogEntryView view, Hashtable args) {
        super(context);
        this.catalogEntryView = view;
        this.args = args;
    }

    public void onCreate() {
        if(this.args.containsKey(ARG_ENTRY_OPDS_STR)) {
            try {
                entryFeed = new UstadJSOPDSFeed();
                entryFeed.loadFromString(args.get(ARG_ENTRY_OPDS_STR).toString());
                entry = entryFeed.entries[0];
                entry.loadFromString(args.get(ARG_ENTRY_OPDS_STR).toString());
                catalogEntryView.setTitle(entry.title);

                CatalogEntryInfo entryInfo = CatalogController.getEntryInfo(entry.id,
                        CatalogController.SHARED_RESOURCE | CatalogController.USER_RESOURCE, context);
                catalogEntryView.setDescription(entry.content, entry.getContentType());

                if(entryInfo != null && entryInfo.acquisitionStatus == CatalogController.STATUS_ACQUIRED) {
                    catalogEntryView.setButtonDisplayed(CatalogEntryView.BUTTON_DOWNLOAD, false);
                }else {
                    catalogEntryView.setButtonDisplayed(CatalogEntryView.BUTTON_OPEN,false);
                    catalogEntryView.setButtonDisplayed(CatalogEntryView.BUTTON_REMOVE,false);
                }

                loadImages();
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void loadImages() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //Load the image icon
                Vector thumbnails = entry.getThumbnails();
                if(thumbnails != null && thumbnails.size() > 0) {
                    try {
                        String thumbnailUrl = UMFileUtil.resolveLink(
                                entryFeed.getAbsoluteSelfLink()[UstadJSOPDSItem.ATTR_HREF],
                                ((String[])thumbnails.get(0))[UstadJSOPDSItem.ATTR_HREF]);
                        final String thumbnailFileUri = UstadMobileSystemImpl.getInstance().getHTTPCacheDir(
                                getContext()).get(thumbnailUrl);

                        catalogEntryView.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                catalogEntryView.setIcon(thumbnailFileUri);
                            }
                        });
                    }catch(IOException e) {
                        e.printStackTrace();
                    }
                }

                Vector coverImages = entry.getLinks(UstadJSOPDSItem.LINK_COVER_IMAGE, null);
                if(coverImages != null && coverImages.size() > 0) {
                    try {
                        String coverImageUrl = UMFileUtil.resolveLink(
                            entryFeed.getAbsoluteSelfLink()[UstadJSOPDSItem.ATTR_HREF],
                                ((String[])coverImages.get(0))[UstadJSOPDSItem.ATTR_HREF]);
                        final String coverImageFileUri = UstadMobileSystemImpl.getInstance().getHTTPCacheDir(
                                getContext()).get(coverImageUrl);
                        catalogEntryView.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                catalogEntryView.setHeader(coverImageFileUri);
                            }
                        });
                    }catch(IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void handleClickButton(int buttonId) {
        switch(buttonId) {
            case CatalogEntryView.BUTTON_DOWNLOAD:
                handleClickDownload(entryFeed);
                break;

            case CatalogEntryView.BUTTON_REMOVE:
                handleClickRemove(new UstadJSOPDSEntry[]{entry});
                break;
            case CatalogEntryView.BUTTON_OPEN:
                handleClickOpenEntry(entry);
                break;

        }
    }

    @Override
    protected void onDownloadStarted() {
        catalogEntryView.setProgressVisible(true);
    }

    @Override
    protected void onEntriesRemoved() {
        catalogEntryView.setButtonDisplayed(CatalogEntryView.BUTTON_DOWNLOAD, true);
        catalogEntryView.setButtonDisplayed(CatalogEntryView.BUTTON_REMOVE, false);
        catalogEntryView.setButtonDisplayed(CatalogEntryView.BUTTON_OPEN, false);
    }

    @Override
    public void setUIStrings() {

    }

    @Override
    public void statusUpdated(AcquisitionStatusEvent event) {
        //TODO: Rework to use the networkmanager instead of acquisition manager
        /*if(event.getEntryId() != null && event.getEntryId().equals(entry.id)) {
            int newStatus = AcquisitionManager.getStringIdForDownloadStatus(event.getStatus());
            if(newStatus != downloadStatusStrId) {
                catalogEntryView.setProgressStatusText(UstadMobileSystemImpl.getInstance().getString(newStatus));
                downloadStatusStrId = newStatus;
            }

            switch(event.getStatus()) {
                case UstadMobileSystemImpl.DLSTATUS_SUCCESSFUL:
                    catalogEntryView.setButtonDisplayed(CatalogEntryView.BUTTON_DOWNLOAD, false);
                    catalogEntryView.setButtonDisplayed(CatalogEntryView.BUTTON_REMOVE, true);
                    catalogEntryView.setButtonDisplayed(CatalogEntryView.BUTTON_OPEN, true);
                    catalogEntryView.setProgressVisible(false);
                    registerItemAcquisitionCompleted(event.getEntryId());
                case UstadMobileSystemImpl.DLSTATUS_RUNNING:
                    catalogEntryView.setProgress(
                            (float)((double)event.getBytesDownloadedSoFar() / (double)event.getTotalBytes()));
                    break;
            }
        }*/
    }

    public void onDestroy() {
    }
}
