package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.AcquisitionManager;
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

public class CatalogEntryPresenter extends UstadBaseController{

    private CatalogEntryView catalogEntryView;

    private Hashtable args;

    public static final String ARG_ENTRY_OPDS_STR = "opds_str";

    private UstadJSOPDSEntry entry;

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
                UstadJSOPDSFeed entryFeed = new UstadJSOPDSFeed();
                entryFeed.loadFromString(args.get(ARG_ENTRY_OPDS_STR).toString());
                entry = entryFeed.entries[0];
                entry.loadFromString(args.get(ARG_ENTRY_OPDS_STR).toString());
                catalogEntryView.setTitle(entry.title);

                CatalogEntryInfo entryInfo = CatalogController.getEntryInfo(entry.id,
                        CatalogController.SHARED_RESOURCE | CatalogController.USER_RESOURCE, context);
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
    }

    public void handleClickButton(int buttonId) {
        switch(buttonId) {
            case CatalogEntryView.BUTTON_DOWNLOAD:
                UMStorageDir[] dirs = UstadMobileSystemImpl.getInstance().getStorageDirs(
                    CatalogController.SHARED_RESOURCE, context);
                String[] parentSelfLink = this.entry.parentFeed.getSelfLink();
                UstadJSOPDSFeed acquireFeed = new UstadJSOPDSFeed(
                        parentSelfLink[UstadJSOPDSEntry.LINK_HREF], "Acquire feed",
                        UMUUID.randomUUID().toString());
                acquireFeed.addEntry(new UstadJSOPDSEntry(acquireFeed, entry));
                acquireFeed.addLink(AcquisitionManager.LINK_REL_DOWNLOAD_DESTINATION, "application/file",
                        dirs[0].getDirURI());
                String[] selfLink = acquireFeed.getSelfLink();
                if(selfLink == null) {
                    acquireFeed.addLink(entry.parentFeed.getSelfLink());
                }
                AcquisitionManager.getInstance().acquireCatalogEntries(acquireFeed, context);
        }
    }


    @Override
    public void setUIStrings() {

    }
}
