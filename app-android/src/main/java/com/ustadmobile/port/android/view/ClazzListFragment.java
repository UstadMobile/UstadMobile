package com.ustadmobile.port.android.view;

import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.os.Bundle;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
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

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

/**
 * ClazzListFragment Android fragment extends UstadBaseFragment
 */
public class ClazzListFragment extends UstadBaseFragment implements ClazzListView{

    private View rootContainer;

    //RecyclerView
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mRecyclerLayoutManager;

    private ClazzListPresenter mPresenter;
    Spinner sortSpinner;
    String[] sortSpinnerPresets;

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

    /**
     * On Create of the fragment.
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * On Create of the View fragment . Part of Android's Fragment Override
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return the root container
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootContainer =
                inflater.inflate(R.layout.fragment_clazz_list, container, false);
        setHasOptionsMenu(true);

        mRecyclerView = rootContainer.findViewById(R.id.fragment_class_list_recyclerview);

        mRecyclerLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);

        sortSpinner = rootContainer.findViewById(R.id.fragment_clazz_list_sort_spinner);

        //set up Presenter
        mPresenter = new ClazzListPresenter(getContext(),
                UMAndroidUtil.bundleToHashtable(getArguments()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        FloatingTextButton fab = rootContainer.findViewById(R.id.fragment_clazz_list_fab);
        fab.setOnClickListener(v -> mPresenter.handleClickPrimaryActionButton());

        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mPresenter.handleChangeSortOrder(id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //return container
        return rootContainer;
    }

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

    @Override
    public void setClazzListProvider(UmProvider<ClazzWithNumStudents> clazzListProvider) {
        ClazzListRecyclerAdapter recyclerAdapter = new ClazzListRecyclerAdapter(DIFF_CALLBACK,
                getContext(), this, mPresenter);

        DataSource.Factory<Integer, ClazzWithNumStudents> factory =
                (DataSource.Factory<Integer, ClazzWithNumStudents>)clazzListProvider.getProvider();
        LiveData<PagedList<ClazzWithNumStudents>> data =
                new LivePagedListBuilder<>(factory, 20).build();
        data.observe(this, recyclerAdapter::submitList);

        mRecyclerView.setAdapter(recyclerAdapter);
    }

    @Override
    public void updateSortSpinner(String[] presets) {
        this.sortSpinnerPresets = presets;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                R.layout.spinner_item, sortSpinnerPresets);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(adapter);
    }

    // This event is triggered soon after onCreateView().
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Setup any handles to view objects here

    }

}
