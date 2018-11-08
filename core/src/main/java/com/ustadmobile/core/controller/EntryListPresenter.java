package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.ContentEntryDetailView;
import com.ustadmobile.core.view.ContentEntryView;
import com.ustadmobile.lib.db.entities.ContentEntry;
import java.util.Hashtable;

public class EntryListPresenter extends UstadBaseController<ContentEntryView> {

    public static final String ARG_CONTENT_ENTRY_UID = "entryid";
    private final ContentEntryView viewContract;
    private ContentEntryDao contentEntryDao;

    public EntryListPresenter(Object context, Hashtable arguments, ContentEntryView viewContract) {
        super(context, arguments, viewContract);
        this.viewContract = viewContract;

    }


    public void onCreate(Hashtable hashtable) {
        UmAppDatabase appDatabase = UmAppDatabase.getInstance(getContext());
        contentEntryDao = appDatabase.getContentEntryDao();
        Long parentUid = (Long) getArguments().get(ARG_CONTENT_ENTRY_UID);
        viewContract.setContentEntryProvider(contentEntryDao.getChildrenByParentUid(parentUid));
        contentEntryDao.getContentByUuid(parentUid, new UmCallback<ContentEntry>() {
            @Override
            public void onSuccess(ContentEntry result) {
                viewContract.setToolbarTitle(result.getTitle());
            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });

    }


    @Override
    public void setUIStrings() {

    }

    public void handleContentEntryClicked(ContentEntry entry) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        Long entryUid = entry.getContentEntryUid();

        contentEntryDao.getCountNumberOfChildrenByParentUUid(entryUid, new UmCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                if(result > 0){
                    args.put(ARG_CONTENT_ENTRY_UID, entryUid);
                    impl.go(ContentEntryView.VIEW_NAME, args, view.getContext());
                }else{
                    args.put(ARG_CONTENT_ENTRY_UID, entryUid);
                    impl.go(ContentEntryDetailView.VIEW_NAME, args, view.getContext());
                }
            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });

    }
}
