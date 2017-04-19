package com.ustadmobile.core.controller;

import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.view.CatalogEntryView;

import java.util.Hashtable;

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
                entry = new UstadJSOPDSEntry(null);
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

        }
    }


    @Override
    public void setUIStrings() {

    }
}
