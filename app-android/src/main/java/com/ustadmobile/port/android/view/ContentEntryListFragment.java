package com.ustadmobile.port.android.view;

import android.Manifest;
import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.networkmanager.LocalAvailabilityMonitor;
import com.ustadmobile.core.view.ContentEntryListFragmentView;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryWithStatusAndMostRecentContainerUid;
import com.ustadmobile.lib.db.entities.DistinctCategorySchema;
import com.ustadmobile.lib.db.entities.Language;
import com.ustadmobile.port.android.netwokmanager.NetworkManagerAndroidBle;

import java.util.List;
import java.util.Map;

import static com.ustadmobile.port.android.util.UMAndroidUtil.bundleToMap;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link}
 * interface.
 */
public class ContentEntryListFragment extends UstadBaseFragment implements ContentEntryListFragmentView,
        ContentEntryListRecyclerViewAdapter.AdapterViewListener, LocalAvailabilityMonitor {


    private ContentEntryListPresenter entryListPresenter;

    private RecyclerView recyclerView;

    private ContentEntryListener contentEntryListener;

    private UstadBaseActivity ustadBaseActivity;

    private NetworkManagerAndroidBle managerAndroidBle;

    private ContentEntryListRecyclerViewAdapter recyclerAdapter;

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

    @Override
    public void setCategorySchemaSpinner(Map<Long, ? extends List<? extends DistinctCategorySchema>> spinnerData) {
        runOnUiThread(() -> {
            if (contentEntryListener != null) {
                // TODO tell activiity to create the spinners
                contentEntryListener.setFilterSpinner((Map<Long, List<DistinctCategorySchema>>) spinnerData);
            }
        });
    }

    @Override
    public void setLanguageOptions(List<? extends Language> result) {
        runOnUiThread(() -> {
            if (contentEntryListener != null) {
                contentEntryListener.setLanguageFilterSpinner((List<Language>) result);
            }
        });
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
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
                bundleToMap(getArguments()), this);
        entryListPresenter.onCreate(bundleToMap(savedInstanceState));

        return rootContainer;
    }


    @Override
    public void onAttach(Context context) {
        if(context instanceof UstadBaseActivity){
            this.ustadBaseActivity = ((UstadBaseActivity)context);
            ustadBaseActivity.runAfterServiceConnection( ()-> {
                managerAndroidBle = (NetworkManagerAndroidBle)ustadBaseActivity
                        .getNetworkManagerBle();
                recyclerAdapter.setNetworkManager(managerAndroidBle);
            });
        }

        if (context instanceof ContentEntryListener) {
            this.contentEntryListener = (ContentEntryListener) context;
        }
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.contentEntryListener = null;
        this.ustadBaseActivity = null;
    }

    @Override
    public void setContentEntryProvider(UmProvider<ContentEntryWithStatusAndMostRecentContainerUid> entryProvider) {
        recyclerAdapter = new ContentEntryListRecyclerViewAdapter(getActivity(),this, this);
        DataSource.Factory<Integer, ContentEntryWithStatusAndMostRecentContainerUid> factory =
                (DataSource.Factory<Integer, ContentEntryWithStatusAndMostRecentContainerUid>) entryProvider.getProvider();
        LiveData<PagedList<ContentEntryWithStatusAndMostRecentContainerUid>> data =
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
    public void contentEntryClicked(ContentEntry entry) {
        runOnUiThread(() -> {
            if(entryListPresenter != null){
                entryListPresenter.handleContentEntryClicked(entry);
            }
        });
    }

    @Override
    public void downloadStatusClicked(ContentEntry entry) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.Companion.getInstance();
        ustadBaseActivity.runAfterGrantingPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                () -> entryListPresenter.handleDownloadStatusButtonClicked(entry),
                impl.getString(MessageID.download_storage_permission_title,getContext()),
                impl.getString(MessageID.download_storage_permission_message,getContext()));
    }

    @Override
    public void startMonitoringAvailability(Object monitor, List<Long> containerUidsToMonitor) {
         new Thread(() -> {
             if(managerAndroidBle != null){
                 managerAndroidBle.startMonitoringAvailability(monitor,containerUidsToMonitor);
             }
         }).start();
    }

    @Override
    public void stopMonitoringAvailability(Object monitor) {
        if(managerAndroidBle != null){
            managerAndroidBle.stopMonitoringAvailability(monitor);
        }
    }

    @Override
    public void onStop() {
        stopMonitoringAvailability(this);
        super.onStop();
    }

}
