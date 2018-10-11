package com.ustadmobile.port.android.view;

import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.os.Bundle;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.SELSelectStudentPresenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.SELSelectStudentView;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.port.android.util.UMAndroidUtil;

public class SELSelectStudentActivity extends UstadBaseActivity implements SELSelectStudentView
{

    private Toolbar toolbar;

    //RecyclerView
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mRecyclerLayoutManager;
    private RecyclerView.Adapter mAdapter; //replaced with object in set view provider method.
    private SELSelectStudentPresenter mPresenter;

    public long clazzUid;

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
    public void setSELAnswerListProvider(UmProvider<Person> selStudentsProvider) {

        // Specify the mAdapter
        SimplePeopleListRecyclerAdapter recyclerAdapter =
                new SimplePeopleListRecyclerAdapter(DIFF_CALLBACK, getApplicationContext());

        // get the provider, set , observe, etc.
        DataSource.Factory<Integer, Person> factory =
                (DataSource.Factory<Integer, Person>)
                        selStudentsProvider.getProvider();
        LiveData<PagedList<Person>> data =
                new LivePagedListBuilder<>(factory, 20).build();
        //Observe the data:
        data.observe(this, recyclerAdapter::submitList);

        //set the adapter
        mRecyclerView.setAdapter(recyclerAdapter);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        //Setting layout:
        setContentView(R.layout.activity_sel_select_student);

        //Toolbar:
        toolbar = findViewById(R.id.activity_sel_select_student_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Recycler View:
        mRecyclerView = (RecyclerView) findViewById(
                R.id.activity_sel_select_student_recyclerview);
        mRecyclerLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);

        //Call the Presenter
        mPresenter = new SELSelectStudentPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

    }


}
