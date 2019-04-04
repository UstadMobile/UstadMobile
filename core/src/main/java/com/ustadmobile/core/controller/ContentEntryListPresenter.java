package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.ContentEntryDetailView;
import com.ustadmobile.core.view.ContentEntryListView;
import com.ustadmobile.core.view.DummyView;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.DistinctCategorySchema;
import com.ustadmobile.lib.db.entities.Language;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class ContentEntryListPresenter extends UstadBaseController<ContentEntryListView> {

    public static final String ARG_CONTENT_ENTRY_UID = "entryid";

    public static final String ARG_DOWNLOADED_CONTENT = "downloaded";

    private final ContentEntryListView viewContract;

    private ContentEntryDao contentEntryDao;

    private long filterByLang = 0;

    private long filterByCategory = 0;

    private Long parentUid;

    public ContentEntryListPresenter(Object context, HashMap<String , String> arguments, ContentEntryListView viewContract) {
        super(context, arguments, viewContract);
        this.viewContract = viewContract;

    }

    public void onCreate(Hashtable hashtable) {
        UmAppDatabase appDatabase = UmAccountManager.getRepositoryForActiveAccount(getContext());
        contentEntryDao = appDatabase.getContentEntryDao();

        if(getArguments().containsKey(ARG_CONTENT_ENTRY_UID)) {
            showContentByParent();
        }else if(getArguments().containsKey(ARG_DOWNLOADED_CONTENT)) {
            showDownloadedContent();
        }
    }

    private void showContentByParent() {
        parentUid = Long.valueOf(getArguments().get(ARG_CONTENT_ENTRY_UID));
        viewContract.setContentEntryProvider(contentEntryDao.getChildrenByParentUidWithCategoryFilter(parentUid, 0, 0));
        contentEntryDao.getContentByUuid(parentUid, new UmCallback<ContentEntry>() {
            @Override
            public void onSuccess(ContentEntry result) {
                if (result == null) {
                    viewContract.runOnUiThread(viewContract::showError);
                    return;
                }
                viewContract.setToolbarTitle(result.getTitle());
            }

            @Override
            public void onFailure(Throwable exception) {
                viewContract.runOnUiThread(viewContract::showError);
            }
        });

        contentEntryDao.findUniqueLanguagesInList(parentUid, new UmCallback<List<Language>>() {
            @Override
            public void onSuccess(List<Language> result) {
                if (result != null && result.size() > 1) {
                    Language selectLang = new Language();
                    selectLang.setName("Language");
                    selectLang.setLangUid(0);
                    result.add(0, selectLang);

                    Language allLang = new Language();
                    allLang.setName("All");
                    allLang.setLangUid(0);
                    result.add(1, allLang);

                    viewContract.setLanguageOptions(result);
                }
            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });

        contentEntryDao.findListOfCategories(parentUid, new UmCallback<List<DistinctCategorySchema>>() {
            @Override
            public void onSuccess(List<DistinctCategorySchema> result) {
                if (result != null && !result.isEmpty()) {

                    Map<Long, List<DistinctCategorySchema>> schemaMap = new HashMap<>();
                    for (DistinctCategorySchema schema : result) {
                        List<DistinctCategorySchema> data = schemaMap.get(schema.getContentCategorySchemaUid());
                        if (data == null) {
                            data = new ArrayList<>();
                            DistinctCategorySchema schemaTitle = new DistinctCategorySchema();
                            schemaTitle.setCategoryName(schema.getSchemaName());
                            schemaTitle.setContentCategoryUid(0);
                            schemaTitle.setContentCategorySchemaUid(0);
                            data.add(0, schemaTitle);

                            DistinctCategorySchema allSchema = new DistinctCategorySchema();
                            allSchema.setCategoryName("All");
                            allSchema.setContentCategoryUid(0);
                            allSchema.setContentCategorySchemaUid(0);
                            data.add(1, allSchema);

                        }
                        data.add(schema);
                        schemaMap.put(schema.getContentCategorySchemaUid(), data);
                    }

                    viewContract.setCategorySchemaSpinner(schemaMap);
                }

            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });
    }

    private void showDownloadedContent() {
        viewContract.setContentEntryProvider(contentEntryDao.getDownloadedRootItems());
    }


    public void handleContentEntryClicked(ContentEntry entry) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        Long entryUid = entry.getContentEntryUid();

        contentEntryDao.findByUid(entryUid, new UmCallback<ContentEntry>() {
            @Override
            public void onSuccess(ContentEntry result) {
                if (result == null) {
                    viewContract.runOnUiThread(viewContract::showError);
                    return;
                }

                if (result.isLeaf()) {
                    args.put(ARG_CONTENT_ENTRY_UID, String.valueOf(entryUid));
                    impl.go(ContentEntryDetailView.VIEW_NAME, args, view.getContext());
                } else {
                    args.put(ARG_CONTENT_ENTRY_UID, String.valueOf(entryUid));
                    impl.go(ContentEntryListView.VIEW_NAME, args, view.getContext());
                }
            }

            @Override
            public void onFailure(Throwable exception) {
                viewContract.runOnUiThread(viewContract::showError);
            }
        });
    }

    public void handleClickFilterByLanguage(long langUid) {
        this.filterByLang = langUid;
        viewContract.setContentEntryProvider(contentEntryDao.getChildrenByParentUidWithCategoryFilter
                (parentUid, filterByLang, filterByCategory));
    }

    public void handleClickFilterByCategory(long contentCategoryUid) {
        this.filterByCategory = contentCategoryUid;
        viewContract.setContentEntryProvider(contentEntryDao.getChildrenByParentUidWithCategoryFilter
                (parentUid, filterByLang, filterByCategory));
    }

    public void handleUpNavigation() {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        impl.go(DummyView.VIEW_NAME, null, view.getContext(),
                UstadMobileSystemImpl.GO_FLAG_CLEAR_TOP | UstadMobileSystemImpl.GO_FLAG_SINGLE_TOP);

    }

    public void handleDownloadStatusButtonClicked(ContentEntry entry) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable<String, String> args = new Hashtable<>();
        args.put("contentEntryUid", String.valueOf(entry.getContentEntryUid()));
        impl.go("DownloadDialog", args, getContext());
    }
}
