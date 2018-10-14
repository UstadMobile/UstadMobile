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

import com.github.clans.fab.FloatingActionButton;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.PeopleListPresenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.PeopleListView;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

/**
 * PeopleListFragment Android fragment extends UstadBaseFragment
 */
public class PeopleListFragment extends UstadBaseFragment implements PeopleListView,
        View.OnClickListener, View.OnLongClickListener {

    View rootContainer;
    //RecyclerView
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mRecyclerLayoutManager;
    private RecyclerView.Adapter mAdapter;
    private PeopleListPresenter mPresenter;

    /**
     * Generates a new Fragment for a page fragment
     *
     * @return A new instance of fragment PeopleListFragment.
     */
    public static PeopleListFragment newInstance() {
        PeopleListFragment fragment = new PeopleListFragment();
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
                inflater.inflate(R.layout.fragment_people_list, container, false);
        setHasOptionsMenu(true);

        mRecyclerView = rootContainer.findViewById(R.id.fragment_people_list_recyclerview);
        mRecyclerLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);

        //set up Presenter
        mPresenter = new PeopleListPresenter(this,
                UMAndroidUtil.bundleToHashtable(getArguments()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        FloatingTextButton fab = rootContainer.findViewById(R.id.fragment_people_list_fab);
        fab.setOnClickListener(v -> mPresenter.handleClickPrimaryActionButton());


        //return container
        return rootContainer;
    }

    // This event is triggered soon after onCreateView().
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Setup any handles to view objects here

    }

    public static final DiffUtil.ItemCallback<Person> DIFF_CALLBACK =
        new DiffUtil.ItemCallback<Person>() {
            @Override
            public boolean areItemsTheSame(Person oldItem,
                                           Person newItem) {
                return oldItem.getPersonUid() == newItem.getPersonUid();
            }

            @Override
            public boolean areContentsTheSame(Person oldItem,
                                              Person newItem) {
                return oldItem.equals(newItem);
            }
        };

    @Override
    public void onClick(View v) {

    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }

    @Override
    public void setListProvider(UmProvider<Person> listProvider) {
        SimplePeopleListRecyclerAdapter recyclerAdapter =
                new SimplePeopleListRecyclerAdapter(DIFF_CALLBACK, getContext(), mPresenter);
        DataSource.Factory<Integer, Person> factory =
                (DataSource.Factory<Integer, Person>)listProvider.getProvider();
        LiveData<PagedList<Person>> data =
                new LivePagedListBuilder<>(factory, 20).build();
        data.observe(this, recyclerAdapter::submitList);

        mRecyclerView.setAdapter(recyclerAdapter);
    }
}
