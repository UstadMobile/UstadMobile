package com.ustadmobile.port.android.view;

import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.support.v7.util.DiffUtil;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.Person;

import com.ustadmobile.core.controller.SELSelectConsentPresenter;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.ustadmobile.port.android.util.UMAndroidUtil;
import com.toughra.ustadmobile.R;


import com.ustadmobile.core.view.SELSelectConsentView;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;


/**
 * The SELSelectConsent activity.
 * <p>
 * This Activity extends UstadBaseActivity and implements SELSelectConsentView
 */
public class SELSelectConsentActivity extends UstadBaseActivity implements SELSelectConsentView {

    private Toolbar toolbar;

    //RecyclerView
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mRecyclerLayoutManager;
    private RecyclerView.Adapter mAdapter; //replaced with object in set view provider method.
    private SELSelectConsentPresenter mPresenter;

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

    @Override
    public void setListProvider(UmProvider<Person> listProvider) {

        // Specify the mAdapter
        SimplePeopleListRecyclerAdapter recyclerAdapter =
                new SimplePeopleListRecyclerAdapter(DIFF_CALLBACK, getApplicationContext());

        // get the provider, set , observe, etc.
        DataSource.Factory<Integer, Person> factory =
                (DataSource.Factory<Integer, Person>)
                        listProvider.getProvider();
        LiveData<PagedList<Person>> data =
                new LivePagedListBuilder<>(factory, 20).build();
        //Observe the data:
        data.observe(this, recyclerAdapter::submitList);

        //set the adapter
        mRecyclerView.setAdapter(recyclerAdapter);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setting layout:
        setContentView(R.layout.activity_selselect_consent);

        //Toolbar:
        toolbar = findViewById(R.id.activity_selselect_consent_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Recycler View:
        mRecyclerView = (RecyclerView) findViewById(
                R.id.activity_selselect_consent_recyclerview);
        mRecyclerLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);

        //Call the Presenter
        mPresenter = new SELSelectConsentPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        //FAB:
        FloatingTextButton fab = findViewById(R.id.activity_selselect_consent_fab);
        fab.setOnClickListener(v -> mPresenter.handleClickPrimaryActionButton(-1L));

    }


}
