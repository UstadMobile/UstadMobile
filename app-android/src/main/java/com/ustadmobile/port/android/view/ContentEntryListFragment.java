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
import com.ustadmobile.core.view.ContentEntryView;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.port.android.util.UMAndroidUtil;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link}
 * interface.
 */
public class ContentEntryListFragment extends UstadBaseFragment implements ContentEntryView, ContentEntryRecyclerViewAdapter.AdapterViewListener {


    private ContentEntryListPresenter entryListPresenter;
    private RecyclerView recyclerView;
    private ContentEntryListener contentEntryListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ContentEntryListFragment() {
    }


    public interface ContentEntryListener {
        void setTitle(String title);
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
        recyclerView =  rootContainer.findViewById(R.id.content_entry_list);
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
        super.onAttach(context);
        if(context instanceof ContentEntryListener) {
            this.contentEntryListener = (ContentEntryListener) context;
        }
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
        if(contentEntryListener != null)
            contentEntryListener.setTitle(title);
    }

    @Override
    public void showError() {
        Toast.makeText(getContext(), R.string.content_entry_not_found, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void contentEntryClicked(ContentEntry entry) {
        entryListPresenter.handleContentEntryClicked(entry);
    }

}
