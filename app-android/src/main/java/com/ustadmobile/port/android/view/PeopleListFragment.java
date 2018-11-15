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

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.PeopleListPresenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.PeopleListView;
import com.ustadmobile.lib.db.entities.PersonWithEnrollment;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

/**
 * PeopleListFragment responsible for showing people list on the people bottom navigation 
 */
public class PeopleListFragment extends UstadBaseFragment implements PeopleListView {

    View rootContainer;
    private RecyclerView mRecyclerView;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * On Create of the View fragment. Sets up the presenter and the floating action button's
     * on click listener.
     *
     * @param inflater              The inflater
     * @param container             The view group container
     * @param savedInstanceState    The saved instance state
     * @return                      The view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootContainer =
                inflater.inflate(R.layout.fragment_people_list, container, false);
        setHasOptionsMenu(true);

        mRecyclerView = rootContainer.findViewById(R.id.fragment_people_list_recyclerview);
        RecyclerView.LayoutManager mRecyclerLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);

        //set up Presenter
        mPresenter = new PeopleListPresenter(this,
                UMAndroidUtil.bundleToHashtable(getArguments()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        FloatingTextButton fab = rootContainer.findViewById(R.id.fragment_people_list_fab);
        fab.setOnClickListener(v -> mPresenter.handleClickPrimaryActionButton());

        return rootContainer;
    }

    /**
     * The DIFF CALLBACK
     */
    public static final DiffUtil.ItemCallback<PersonWithEnrollment> DIFF_CALLBACK2 =
            new DiffUtil.ItemCallback<PersonWithEnrollment>() {
                @Override
                public boolean areItemsTheSame(PersonWithEnrollment oldItem,
                                               PersonWithEnrollment newItem) {
                    return oldItem.getPersonUid() == newItem.getPersonUid();
                }

                @Override
                public boolean areContentsTheSame(PersonWithEnrollment oldItem,
                                                  PersonWithEnrollment newItem) {
                    return oldItem.equals(newItem);
                }
            };

    @Override
    public void setPeopleListProvider(UmProvider<PersonWithEnrollment> listProvider) {

        PersonWithEnrollmentRecyclerAdapter recyclerAdapter =
                new PersonWithEnrollmentRecyclerAdapter(DIFF_CALLBACK2, getContext(),
                        this, mPresenter, false, false);
        //A warning is expected
        DataSource.Factory<Integer, PersonWithEnrollment> factory =
                (DataSource.Factory<Integer, PersonWithEnrollment>)listProvider.getProvider();
        LiveData<PagedList<PersonWithEnrollment>> data =
                new LivePagedListBuilder<>(factory, 20).build();
        data.observe(this, recyclerAdapter::submitList);

        mRecyclerView.setAdapter(recyclerAdapter);

    }
}
