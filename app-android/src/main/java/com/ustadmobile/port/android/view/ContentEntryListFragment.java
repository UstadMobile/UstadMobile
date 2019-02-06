package com.ustadmobile.port.android.view;

import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ContentEntryListPresenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.ContentEntryListView;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.DistinctCategorySchema;
import com.ustadmobile.lib.db.entities.Language;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.List;
import java.util.Map;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link}
 * interface.
 */
public class ContentEntryListFragment extends UstadBaseFragment implements ContentEntryListView, ContentEntryRecyclerViewAdapter.AdapterViewListener {


    private ContentEntryListPresenter entryListPresenter;
    private RecyclerView recyclerView;
    private ContentEntryListener contentEntryListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ContentEntryListFragment() {
    }

    public void filterByLang(long langUid) {
        entryListPresenter.handleClickFilterByLanguage(langUid);
    }

    public void filterBySchemaCategory(long contentCategoryUid, long contentCategorySchemaUid) {
        entryListPresenter.handleClickFilterByCategory(contentCategoryUid);
    }

    public void clickUpNavigation() {
       entryListPresenter.handleUpNavigation();
    }

    public interface ContentEntryListener {
        void setTitle(String title);

        void setFilterSpinner(Map<Long, List<DistinctCategorySchema>> idToValuesMap);

        void setLanguageFilterSpinner(List<Language> result);
    }


    @SuppressWarnings("unused")
    public static ContentEntryListFragment newInstance(Bundle args) {
        ContentEntryListFragment fragment = new ContentEntryListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootContainer = inflater.inflate(R.layout.fragment_contententry_list, container, false);

        // Set the adapter
        Context context = rootContainer.getContext();
        recyclerView = rootContainer.findViewById(R.id.content_entry_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(context,
                LinearLayoutManager.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        entryListPresenter = new ContentEntryListPresenter(getContext(),
                UMAndroidUtil.bundleToHashtable(getArguments()), this);
        entryListPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        return rootContainer;
    }


    @Override
    public void onAttach(Context context) {
        if (context instanceof ContentEntryListener) {
            this.contentEntryListener = (ContentEntryListener) context;
        }

        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.contentEntryListener = null;
    }

    @Override
    public void setContentEntryProvider(UmProvider<ContentEntry> entryProvider) {
        ContentEntryRecyclerViewAdapter recyclerAdapter = new ContentEntryRecyclerViewAdapter(this);
        DataSource.Factory<Integer, ContentEntry> factory =
                (DataSource.Factory<Integer, ContentEntry>) entryProvider.getProvider();
        LiveData<PagedList<ContentEntry>> data =
                new LivePagedListBuilder<>(factory, 20).build();
        data.observe(this, recyclerAdapter::submitList);

        recyclerView.setAdapter(recyclerAdapter);
    }

    @Override
    public void setToolbarTitle(String title) {
        runOnUiThread(() -> {
            if (contentEntryListener != null)
                contentEntryListener.setTitle(title);
        });
    }

    @Override
    public void showError() {
        Toast.makeText(getContext(), R.string.content_entry_not_found, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setCategorySchemaSpinner(Map<Long, List<DistinctCategorySchema>> spinnerData) {
        runOnUiThread(() -> {
            if (contentEntryListener != null) {
                // TODO tell activiity to create the spinners
                contentEntryListener.setFilterSpinner(spinnerData);
            }
        });
    }

    @Override
    public void setLanguageOptions(List<Language> result) {
        runOnUiThread(() -> {
            if (contentEntryListener != null) {
                contentEntryListener.setLanguageFilterSpinner(result);
            }
        });
    }

    @Override
    public void contentEntryClicked(ContentEntry entry) {
        runOnUiThread(() -> {
            if(entryListPresenter != null){
                entryListPresenter.handleContentEntryClicked(entry);
            }
        });
    }

}
