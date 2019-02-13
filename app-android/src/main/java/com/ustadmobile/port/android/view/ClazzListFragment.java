package com.ustadmobile.port.android.view;

import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ClazzListPresenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.ClazzListView;
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Objects;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

/**
 * ClazzListFragment Android fragment extends UstadBaseFragment
 */
public class ClazzListFragment extends UstadBaseFragment implements ClazzListView{

    //RecyclerView
    private RecyclerView mRecyclerView;

    private ClazzListPresenter mPresenter;
    Spinner sortSpinner;
    String[] sortSpinnerPresets;
    FloatingTextButton fab;

    private Menu mOptionsMenu;

    private boolean showAllClazzSettingsButton = false;

    /**
     * Generates a new Fragment for a page fragment
     *
     * @return A new instance of fragment ClazzListFragment.
     */
    public static ClazzListFragment newInstance() {
        ClazzListFragment fragment = new ClazzListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public void searchClasses(String searchValue){
        mPresenter.updateProviderWithSearch(searchValue);
    }
    /**
     * On Create of the fragment.
     *
     * @param savedInstanceState    The bundle state
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootContainer = inflater.inflate(R.layout.fragment_clazz_list, container, false);
        setHasOptionsMenu(true);

        mRecyclerView = rootContainer.findViewById(R.id.fragment_class_list_recyclerview);

        RecyclerView.LayoutManager mRecyclerLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);

        sortSpinner = rootContainer.findViewById(R.id.fragment_clazz_list_sort_spinner);

        fab = rootContainer.findViewById(R.id.fragment_clazz_list_fab);
        fab.setOnClickListener(v -> mPresenter.handleClickPrimaryActionButton());

        //set up Presenter
        mPresenter = new ClazzListPresenter(getContext(),
                UMAndroidUtil.bundleToHashtable(getArguments()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mPresenter.handleChangeSortOrder(id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return rootContainer;
    }

    /**
     * The DIFF Callback.
     */
    public static final DiffUtil.ItemCallback<ClazzWithNumStudents> DIFF_CALLBACK =
        new DiffUtil.ItemCallback<ClazzWithNumStudents>() {
            @Override
            public boolean areItemsTheSame(ClazzWithNumStudents oldItem,
                                           ClazzWithNumStudents newItem) {
                return oldItem.getClazzUid() == newItem.getClazzUid();
            }

            @Override
            public boolean areContentsTheSame(ClazzWithNumStudents oldItem,
                                              ClazzWithNumStudents newItem) {
                return oldItem.equals(newItem);
            }
        };

    /**
     * Sets the provider to the view.
     *
     * @param clazzListProvider The UMProvider provider of ClazzWithNumStudents Type.
     */
    @Override
    public void setClazzListProvider(UmProvider<ClazzWithNumStudents> clazzListProvider) {
        ClazzListRecyclerAdapter recyclerAdapter = new ClazzListRecyclerAdapter(DIFF_CALLBACK,
                getContext(), this, mPresenter);

        // a warning is expected.
        DataSource.Factory<Integer, ClazzWithNumStudents> factory =
                (DataSource.Factory<Integer, ClazzWithNumStudents>)clazzListProvider.getProvider();
        LiveData<PagedList<ClazzWithNumStudents>> data =
                new LivePagedListBuilder<>(factory, 20).build();
        data.observe(this, recyclerAdapter::submitList);

        mRecyclerView.setAdapter(recyclerAdapter);
    }

    /**
     * Updates the sort spinner with string list given
     *
     * @param presets A String array String[] of the presets available.
     */
    @Override
    public void updateSortSpinner(String[] presets) {
        this.sortSpinnerPresets = presets;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(Objects.requireNonNull(getContext()),
                R.layout.spinner_item, sortSpinnerPresets);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(adapter);
    }

    @Override
    public void showAddClassButton(boolean show) {
        runOnUiThread(() -> {
            if(show){
                fab.setVisibility(View.VISIBLE);
            }else{
                fab.setVisibility(View.INVISIBLE);
            }
        });

    }

    @Override
    public void showAllClazzSettingsButton(boolean show) {
        showAllClazzSettingsButton = show;
    }

    @Override
    public void forceCheckPermissions() {
        if(mPresenter != null)
            mPresenter.checkPermissions();
    }

    public void showSettings(){

        MenuItem allClazzSettingsMenuItem = mOptionsMenu.findItem(R.id.menu_settings_gear);
        if(allClazzSettingsMenuItem != null){
            allClazzSettingsMenuItem.setVisible(showAllClazzSettingsButton);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        mOptionsMenu = menu;
        showSettings();
    }


}
