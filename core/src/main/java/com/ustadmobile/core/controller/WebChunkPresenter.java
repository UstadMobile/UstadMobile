package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.ContentEntryUtil;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.view.ContentEntryDetailView;
import com.ustadmobile.core.view.ContentEntryListView;
import com.ustadmobile.core.view.DummyView;
import com.ustadmobile.core.view.WebChunkView;
import com.ustadmobile.lib.db.entities.ContentEntry;

import java.util.Hashtable;

import static com.ustadmobile.core.impl.UstadMobileSystemImpl.ARG_REFERRER;
import static com.ustadmobile.core.view.WebChunkView.ARG_CHUNK_PATH;
import static com.ustadmobile.core.view.WebChunkView.ARG_CONTENT_ENTRY_ID;

public class WebChunkPresenter extends UstadBaseController<WebChunkView> {

    private final WebChunkView viewContract;
    private String navigation;

    public WebChunkPresenter(Object context, Hashtable arguments, WebChunkView view) {
        super(context, arguments, view);
        this.viewContract = view;
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);
        UmAppDatabase repoAppDatabase = UmAccountManager.getRepositoryForActiveAccount(getContext());
        ContentEntryDao contentEntryDao = repoAppDatabase.getContentEntryDao();

        Long entryUuid = Long.valueOf((String) getArguments().get(ARG_CONTENT_ENTRY_ID));
        navigation = (String) getArguments().get(ARG_REFERRER);

        contentEntryDao.getContentByUuid(entryUuid, new UmCallback<ContentEntry>() {
            @Override
            public void onSuccess(ContentEntry result) {
                viewContract.setToolbarTitle(result.getTitle());
            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });

        view.mountChunk((String) getArguments().get(ARG_CHUNK_PATH), new UmCallback<String>() {
            @Override
            public void onSuccess(String firstUrl) {
                view.loadUrl(firstUrl);
            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });
    }

    public void handleUrlLinkToContentEntry(String sourceUrl) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        UmAppDatabase repoAppDatabase = UmAccountManager.getRepositoryForActiveAccount(getContext());

        ContentEntryUtil.goToContentEntryByViewDestination(
                sourceUrl,
                repoAppDatabase, impl,
                true,
                getContext(), new UmCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {

                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        viewContract.showError(exception.getMessage());
                    }
                });
    }

    public void handleUpNavigation() {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        String lastEntryListArgs = UMFileUtil.getLastReferrerArgsByViewname(ContentEntryDetailView.VIEW_NAME, navigation);
        if (lastEntryListArgs != null) {
            impl.go(ContentEntryDetailView.VIEW_NAME,
                    UMFileUtil.parseURLQueryString(lastEntryListArgs), view.getContext(),
                    UstadMobileSystemImpl.GO_FLAG_CLEAR_TOP | UstadMobileSystemImpl.GO_FLAG_SINGLE_TOP);
        } else {
            impl.go(DummyView.VIEW_NAME,
                    null, view.getContext(),
                    UstadMobileSystemImpl.GO_FLAG_CLEAR_TOP | UstadMobileSystemImpl.GO_FLAG_SINGLE_TOP);
        }

    }

}
