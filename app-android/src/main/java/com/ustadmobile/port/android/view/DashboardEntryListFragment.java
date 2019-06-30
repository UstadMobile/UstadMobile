package com.ustadmobile.port.android.view;

import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.chip.Chip;
import android.support.design.chip.ChipGroup;
import android.support.v7.app.AlertDialog;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.github.clans.fab.FloatingActionMenu;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.DashboardEntryListPresenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.DashboardEntryListView;
import com.ustadmobile.lib.db.entities.DashboardEntry;
import com.ustadmobile.lib.db.entities.DashboardTag;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Objects;

public class DashboardEntryListFragment
        extends UstadBaseFragment implements DashboardEntryListView {

    View rootContainer;
    private DashboardEntryListPresenter mPresenter;

    private RecyclerView entriesRV;
    private FloatingActionMenu floatingActionMenu;
    private ChipGroup tags;
    private Chip tagAll;

    public static DashboardEntryListFragment newInstance(){
        DashboardEntryListFragment fragment = new DashboardEntryListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        //Inflate view
        rootContainer = inflater.inflate(R.layout.fragment_dashboard_entry_list, container,false);
        setHasOptionsMenu(true);

        //Set recycler views
        //RecyclerView - Entries
        entriesRV = rootContainer.findViewById(
                    R.id.fragment_dashboard_entry_list_entries_rv);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        entriesRV.setLayoutManager(layoutManager);

        tags = rootContainer.findViewById(R.id.fragment_dashboard_entry_list_tags_cg);
        //tagAll = rootContainer.findViewById(R.id.fragment_dashboard_entry_list_chip_all);
        //TODO: Handle tag click

        //Call the Presenter
        mPresenter = new DashboardEntryListPresenter(getContext(),
                UMAndroidUtil.bundleToHashtable(getArguments()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        //Set listeners
        floatingActionMenu = rootContainer.findViewById(R.id.fragment_dashboard_entry_list_fab_menu);
        rootContainer.findViewById(R.id.fragment_dashboard_entry_list_fab_menu_sales_performance)
                .setOnClickListener(v -> {
                    floatingActionMenu.close(true);
                    mPresenter.handleClickNewSalePerformanceReport();
                });

        rootContainer.findViewById(R.id.fragment_dashboard_entry_list_fab_menu_sales_log)
                .setOnClickListener(v -> {
                    floatingActionMenu.close(true);
                    mPresenter.handleClickNewSalesLogReport();
                });
        rootContainer.findViewById(R.id.fragment_dashboard_entry_list_fab_menu_top_les)
                .setOnClickListener(v -> {
                    floatingActionMenu.close(true);
                    mPresenter.handleClickTopLEsReport();
                });

        return rootContainer;
    }


    /**
     * The DIFF CALLBACK
     */
    public static final DiffUtil.ItemCallback<DashboardEntry> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<DashboardEntry>() {
                @Override
                public boolean areItemsTheSame(DashboardEntry oldItem,
                                               DashboardEntry newItem) {
                    return oldItem == newItem;
                }

                @Override
                public boolean areContentsTheSame(DashboardEntry oldItem,
                                                  DashboardEntry newItem) {
                    return oldItem.equals(newItem);
                }
            };

    @Override
    public void finish() {

    }

    @Override
    public void setDashboardEntryProvider(UmProvider<DashboardEntry> listProvider) {

        DashboardEntryListRecyclerAdapter recyclerAdapter =
                new DashboardEntryListRecyclerAdapter(DIFF_CALLBACK, mPresenter,
                        getContext());

        // get the provider, set , observe, etc.
        // A warning is expected
        DataSource.Factory<Integer, DashboardEntry> factory =
                (DataSource.Factory<Integer, DashboardEntry>)
                        listProvider.getProvider();
        LiveData<PagedList<DashboardEntry>> data =
                new LivePagedListBuilder<>(factory, 20).build();
        //Observe the data:
        data.observe(this, recyclerAdapter::submitList);

        //set the adapter
        entriesRV.setAdapter(recyclerAdapter);

    }

    @Override
    public void setDashboardTagProvider(UmProvider<DashboardTag> listProvider) {

        //TODO
    }

    @Override
    public void loadChips(String[] chipNames) {
        for(String chipName:chipNames){
            Chip tag = new Chip(Objects.requireNonNull(getContext()));
            tag.setText(chipName);
            tags.addView(tag);
        }
    }

    @Override
    public void showSetTitle(String existingTitle, long entryUid) {
        String newTitle = "";
        final EditText edittext = new EditText(getContext());
        edittext.setText(existingTitle);

        AlertDialog.Builder adb = new AlertDialog.Builder(getContext())
                .setTitle(getText(R.string.set_title))
                .setMessage("")
                .setView(edittext)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    mPresenter.handleSetTitle(entryUid, edittext.getText().toString());
                    dialog.dismiss();
                })

                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    dialog.dismiss();
                });

        adb.create();
        adb.show();
    }

}
