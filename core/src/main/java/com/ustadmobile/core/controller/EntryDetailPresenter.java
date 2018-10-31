package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryFileDao;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.core.view.ContentEntryDetailView;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryFile;

import java.util.Hashtable;
import java.util.List;

public class EntryDetailPresenter extends UstadBaseController<ContentEntryDetailView> {


    public static final String ARG_CONTENT_ENTRY_UID = "entryid";
    private final ContentEntryDetailView viewContract;
    private ContentEntryFileDao contentFileDao;
    private ContentEntryDao contentEntryDao;

    public EntryDetailPresenter(Object context, Hashtable arguments, ContentEntryDetailView viewContract) {
        super(context, arguments, viewContract);
        this.viewContract = viewContract;

    }

    public void onCreate(Hashtable hashtable) {
        UmAppDatabase appDatabase = UmAppDatabase.getInstance(context);
        contentFileDao = appDatabase.getContentEntryFileDao();
        contentEntryDao = appDatabase.getContentEntryDao();

        long entryUuid = (Long) getArguments().get(ARG_CONTENT_ENTRY_UID);

        contentEntryDao.getContentByUuid(entryUuid, new UmCallback<ContentEntry>() {
            @Override
            public void onSuccess(ContentEntry result) {
                viewContract.setContentInfo(result);
            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });

        contentFileDao.findFilesByContentEntryUid(entryUuid, new UmCallback<List<ContentEntryFile>>() {
            @Override
            public void onSuccess(List<ContentEntryFile> result) {
                viewContract.setFileInfo(result);
            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });

        contentEntryDao.findAllLanguageRelatedEntries(entryUuid, new UmCallback<List<ContentEntry>>() {

            @Override
            public void onSuccess(List<ContentEntry> result) {
                viewContract.setLanguageContent(result);
            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });


    }

    @Override
    public void setUIStrings() {

    }

    public void openTranslation(ContentEntry entry) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        Long entryUid = entry.getContentEntryUid();
        args.put(ARG_CONTENT_ENTRY_UID, entryUid);
        impl.go(ContentEntryDetailView.VIEW_NAME, args, view.getContext());
    }
}
