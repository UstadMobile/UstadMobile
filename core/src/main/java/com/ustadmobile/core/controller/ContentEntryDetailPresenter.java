package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryFileDao;
import com.ustadmobile.core.db.dao.ContentEntryRelatedEntryJoinDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.ContentEntryDetailView;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryFile;
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoinWithLanguage;

import java.util.Hashtable;
import java.util.List;

public class ContentEntryDetailPresenter extends UstadBaseController<ContentEntryDetailView> {


    public static final String ARG_CONTENT_ENTRY_UID = "entryid";
    private final ContentEntryDetailView viewContract;
    private ContentEntryFileDao contentFileDao;
    private ContentEntryDao contentEntryDao;
    private ContentEntryRelatedEntryJoinDao contentRelatedEntryDao;

    public ContentEntryDetailPresenter(Object context, Hashtable arguments, ContentEntryDetailView viewContract) {
        super(context, arguments, viewContract);
        this.viewContract = viewContract;

    }

    public void onCreate(Hashtable hashtable) {
        UmAppDatabase appDatabase = UmAccountManager.getRepositoryForActiveAccount(getContext());
        contentFileDao = appDatabase.getContentEntryFileDao();
        contentRelatedEntryDao = appDatabase.getContentEntryRelatedEntryJoinDao();
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

        contentRelatedEntryDao.findAllTranslationsForContentEntry(entryUuid, new UmCallback<List<ContentEntryRelatedEntryJoinWithLanguage>>() {

            @Override
            public void onSuccess(List<ContentEntryRelatedEntryJoinWithLanguage> result) {
                viewContract.setTranslationsAvailable(result, entryUuid);
            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });


    }

    public void handleClickTranslatedEntry(long uid) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        args.put(ARG_CONTENT_ENTRY_UID, uid);
        impl.go(ContentEntryDetailView.VIEW_NAME, args, view.getContext());
    }
}
