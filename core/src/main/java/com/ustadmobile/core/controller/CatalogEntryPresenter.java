package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.AcquisitionManager;
import com.ustadmobile.core.impl.AcquisitionStatusEvent;
import com.ustadmobile.core.impl.UMStorageDir;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.util.UMUUID;
import com.ustadmobile.core.view.CatalogEntryView;

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

    private int downloadStatusStrId = -1;

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
            }catch(Exception e) {
                e.printStackTrace();
            }
        }

        AcquisitionManager.getInstance().registerEntryAquisitionStatusListener(this, context);

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
        if(event.getEntryId() != null && event.getEntryId().equals(entry.id)) {
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
        }
    }

    public void onDestroy() {
        AcquisitionManager.getInstance().unregisterEntryAquisitionStatusListener(this, context);
    }
}
