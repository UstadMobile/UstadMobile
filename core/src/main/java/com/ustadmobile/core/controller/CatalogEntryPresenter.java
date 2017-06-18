package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.AcquisitionManager;
import com.ustadmobile.core.impl.AcquisitionStatusEvent;
import com.ustadmobile.core.impl.AcquisitionStatusListener;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.networkmanager.AcquisitionListener;
import com.ustadmobile.core.networkmanager.AcquisitionTaskStatus;
import com.ustadmobile.core.networkmanager.EntryCheckResponse;
import com.ustadmobile.core.networkmanager.NetworkManagerCore;
import com.ustadmobile.core.networkmanager.NetworkManagerListener;
import com.ustadmobile.core.networkmanager.NetworkNode;
import com.ustadmobile.core.networkmanager.NetworkTask;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.opds.UstadJSOPDSItem;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.view.CatalogEntryView;


import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

/**
 * Created by mike on 4/17/17.
 */

public class CatalogEntryPresenter extends BaseCatalogController implements AcquisitionListener, NetworkManagerListener{

    private CatalogEntryView catalogEntryView;

    private Hashtable args;

    public static final String ARG_ENTRY_OPDS_STR = "opds_str";

    private UstadJSOPDSEntry entry;

    private UstadJSOPDSFeed entryFeed;

    private NetworkManagerCore manager;

    private long entryCheckTaskId = -1;

    public CatalogEntryPresenter(Object context) {
        super(context);
    }

    public CatalogEntryPresenter(Object context, CatalogEntryView view, Hashtable args) {
        super(context);
        this.catalogEntryView = view;
        this.args = args;
    }

    public void onCreate() {
        manager = UstadMobileSystemImpl.getInstance().getNetworkManager();
        manager.addNetworkManagerListener(this);
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

                updateButtonsByStatus(entryInfo != null ? entryInfo.acquisitionStatus :
                        CatalogController.STATUS_NOT_ACQUIRED);

                loadImages();

                //TODO: as this is bound to the activity - this might not be ready - lifecycle implication needs handled
                NetworkManagerCore manager  = UstadMobileSystemImpl.getInstance().getNetworkManager();
                EntryCheckResponse fileResponse = manager.getEntryResponseWithLocalFile(entry.id);
                if(fileResponse != null) {
                    catalogEntryView.setLocallyAvailableStatus(CatalogEntryView.LOCAL_STATUS_AVAILABLE);
                }else {
                    catalogEntryView.setLocallyAvailableStatus(CatalogEntryView.LOCAL_STATUS_IN_PROGRESS);
                    entryCheckTaskId = manager.requestFileStatus(new String[]{entry.id}, true, true);
                }
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
        UstadMobileSystemImpl.getInstance().getNetworkManager().addAcquisitionTaskListener(this);
    }

    /**
     * Update which buttons are shown according to the acquisition status
     *
     * @param acquisitionStatus
     */
    protected void updateButtonsByStatus(int acquisitionStatus) {
        catalogEntryView.setButtonDisplayed(CatalogEntryView.BUTTON_DOWNLOAD,
                acquisitionStatus != CatalogController.STATUS_ACQUIRED);
        catalogEntryView.setButtonDisplayed(CatalogEntryView.BUTTON_OPEN,
                acquisitionStatus == CatalogController.STATUS_ACQUIRED);
        catalogEntryView.setButtonDisplayed(CatalogEntryView.BUTTON_REMOVE,
                acquisitionStatus == CatalogController.STATUS_ACQUIRED);
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
    public void acquisitionProgressUpdate(String entryId, final AcquisitionTaskStatus status) {
        if(entry != null && entryId.equals(entry.id)) {
            catalogEntryView.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(status.getTotalSize() == -1)
                        catalogEntryView.setProgress(-1);
                    else
                        catalogEntryView.setProgress((float)status.getDownloadedSoFar() / (float)status.getTotalSize());
                }
            });
        }
    }

    @Override
    public void acquisitionStatusChanged(String entryId, AcquisitionTaskStatus status) {
        switch(status.getStatus()) {
            case UstadMobileSystemImpl.DLSTATUS_SUCCESSFUL:
                catalogEntryView.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        catalogEntryView.setProgressVisible(false);
                        updateButtonsByStatus(CatalogController.STATUS_ACQUIRED);
                    }
                });
                break;
            //TODO: handle show download failed
        }
    }

    public void onDestroy() {
        manager.removeNetworkManagerListener(this);
        manager.removeAcquisitionTaskListener(this);
    }

    @Override
    public void fileStatusCheckInformationAvailable(List<String> fileIds) {
        if(fileIds.contains(entry.id)) {
            final boolean available = manager.getEntryResponseWithLocalFile(entry.id) != null;
            updateViewLocallyAvailableStatus(available ?
                    CatalogEntryView.LOCAL_STATUS_AVAILABLE : CatalogEntryView.LOCAL_STATUS_NOT_AVAILABLE);
        }
    }

    @Override
    public void networkTaskCompleted(NetworkTask task) {
        if(task.getTaskId() == entryCheckTaskId) {
            boolean available =
                UstadMobileSystemImpl.getInstance().getNetworkManager().getEntryResponseWithLocalFile(entry.id) != null;
            updateViewLocallyAvailableStatus(available ?
                CatalogEntryView.LOCAL_STATUS_AVAILABLE : CatalogEntryView.LOCAL_STATUS_NOT_AVAILABLE);
        }
    }

    private void updateViewLocallyAvailableStatus(final int status) {
        catalogEntryView.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                catalogEntryView.setLocallyAvailableStatus(status);
            }
        });
    }

    @Override
    public void networkNodeDiscovered(NetworkNode node) {

    }

    @Override
    public void networkNodeUpdated(NetworkNode node) {

    }

    @Override
    public void fileAcquisitionInformationAvailable(String entryId, long downloadId, int downloadSource) {

    }

    @Override
    public void wifiConnectionChanged(String ssid, boolean connected, boolean connectedOrConnecting) {

    }
}
