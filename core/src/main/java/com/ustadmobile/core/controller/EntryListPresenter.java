package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.ContentEntryView;
import com.ustadmobile.lib.db.entities.ContentEntry;

import java.util.Hashtable;

public class EntryListPresenter extends UstadBaseController<ContentEntryView> {

    public static final String ARG_CONTENT_ENTRY_UID = "entryid";
    private final ContentEntryView viewContract;

    public EntryListPresenter(Object context, Hashtable arguments, ContentEntryView viewContract) {
        super(context, arguments, viewContract);
        this.viewContract = viewContract;

    }


    public void onCreate(Hashtable hashtable) {
        UmAppDatabase appDatabase = UmAppDatabase.getInstance(context);
        ContentEntryDao contentEntryDao = appDatabase.getContentEntryDao();
        viewContract.setContentEntryProvider(contentEntryDao.getChildrenByParentUid((Long) getArguments().get(ARG_CONTENT_ENTRY_UID)));
    }


    @Override
    public void setUIStrings() {

    }

    public void handleContentEntryClicked(ContentEntry entry) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        Long entryUid = entry.getContentEntryUid();
        args.put(ARG_CONTENT_ENTRY_UID, entryUid);
        impl.go(ContentEntryView.VIEW_NAME, args, view.getContext());

    }
}
