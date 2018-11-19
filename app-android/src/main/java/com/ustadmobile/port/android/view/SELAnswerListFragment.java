package com.ustadmobile.port.android.view;

import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.SELAnswerListPresenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.SELAnswerListView;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

import static com.ustadmobile.core.view.ClazzListView.ARG_CLAZZ_UID;

/**
 * SELAnswerListFragment Android fragment extends UstadBaseFragment -  is responsible for
 * showing the Answer list (ie: students who have taken the SEL in the SEL tab of Clazz.
 * It should also show a primary action button to record new SEL.
 *
 */
public class SELAnswerListFragment extends UstadBaseFragment implements SELAnswerListView,
        View.OnClickListener, View.OnLongClickListener{

    View rootContainer;
    //RecyclerView
    private RecyclerView mRecyclerView;
    private SELAnswerListPresenter mPresenter;

    public static final DiffUtil.ItemCallback<Person> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Person>() {
                @Override
                public boolean areItemsTheSame(Person oldItem,
                                               Person newItem) {
                    return oldItem == newItem;
                }

                @Override
                public boolean areContentsTheSame(Person oldItem,
                                                  Person newItem) {
                    return oldItem.equals(newItem);
                }
            };

    /**
     * Generates a new Fragment for a page fragment
     *
     * @return A new instance of fragment SELAnswerListFragment.
     */
    public static SELAnswerListFragment newInstance(long clazzUid) {
        SELAnswerListFragment fragment = new SELAnswerListFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_CLAZZ_UID, clazzUid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void setSELAnswerListProvider(UmProvider<Person> selAnswersProvider) {
        // Specify the mAdapter
        SimplePeopleListRecyclerAdapter recyclerAdapter = new SimplePeopleListRecyclerAdapter(
                DIFF_CALLBACK, getContext(),this, mPresenter);

        // get the provider, set , observe, etc.
        // A warning is expected
        DataSource.Factory<Integer, Person> factory =
                (DataSource.Factory<Integer, Person>)
                        selAnswersProvider.getProvider();
        LiveData<PagedList<Person>> data =
                new LivePagedListBuilder<>(factory, 20).build();
        //Observe the data:
        data.observe(this, recyclerAdapter::submitList);

        //set the adapter
        mRecyclerView.setAdapter(recyclerAdapter);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        rootContainer = inflater.inflate(R.layout.fragment_sel_answer_list,
                container, false);
        setHasOptionsMenu(true);

        // Set mRecyclerView..
        mRecyclerView = rootContainer.findViewById(R.id.fragment_sel_answer_list_recyclerview);

        // Use Layout: set layout manager. Change defaults
        RecyclerView.LayoutManager mRecyclerLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                mRecyclerView.getContext(), LinearLayoutManager.VERTICAL);
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        // Set the presenter
        mPresenter = new SELAnswerListPresenter(this,
                UMAndroidUtil.bundleToHashtable(getArguments()),this);

        // Call Presenter's onCreate:
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        //FAB:
        FloatingTextButton fab =
                rootContainer.findViewById(R.id.fragment_sel_answer_list_record_sel_fab);
        //FAB's onClickListener:
        fab.setOnClickListener(v -> mPresenter.handleClickRecordSEL());

        return rootContainer;
    }


    @Override
    public void onClick(View v) {

    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }
}
