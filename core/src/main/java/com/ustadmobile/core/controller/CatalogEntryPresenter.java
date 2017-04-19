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
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void setUIStrings() {

    }
}
