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
import com.ustadmobile.core.controller.PersonDetailEnrollClazzPresenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.PersonDetailEnrollClazzView;
import com.ustadmobile.lib.db.entities.ClazzWithEnrollment;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Objects;

public class PersonDetailEnrollClazzActivity extends UstadBaseActivity implements PersonDetailEnrollClazzView {

    private Toolbar toolbar;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mRecyclerLayoutManager;

    private PersonDetailEnrollClazzPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set layout
        setContentView(R.layout.activity_clazz_list_enroll_person);

        //Toolbar
        toolbar = findViewById(R.id.activity_clazz_list_enroll_person_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mRecyclerView = findViewById(
                R.id.activity_clazz_list_enroll_person_recyclerview);
        mRecyclerLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);

        //Call the presenter
        mPresenter = new PersonDetailEnrollClazzPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        //FAB
        findViewById(R.id.activity_clazz_list_enroll_person_fab)
                .setOnClickListener(v -> mPresenter.handleClickDone());

    }

    public static final DiffUtil.ItemCallback<ClazzWithEnrollment> DIFF_CALLBACK =
        new DiffUtil.ItemCallback<ClazzWithEnrollment>() {
            @Override
            public boolean areItemsTheSame(ClazzWithEnrollment oldItem,
                                           ClazzWithEnrollment newItem) {
                return oldItem.getClazzUid() == newItem.getClazzUid();
            }

            @Override
            public boolean areContentsTheSame(ClazzWithEnrollment oldItem,
                                              ClazzWithEnrollment newItem) {
                return oldItem.equals(newItem);
            }
        };

    @Override
    public void setClazzListProvider(UmProvider<ClazzWithEnrollment> clazzListProvider) {

        ClazzListEnrollPersonRecyclerAdapter recyclerAdapter =
                new ClazzListEnrollPersonRecyclerAdapter(DIFF_CALLBACK, getApplicationContext(),
                        this, mPresenter);
        DataSource.Factory<Integer, ClazzWithEnrollment> factory =
                (DataSource.Factory<Integer, ClazzWithEnrollment>)clazzListProvider.getProvider();
        LiveData<PagedList<ClazzWithEnrollment>> data =
                new LivePagedListBuilder<>(factory, 20).build();
        data.observe(this, recyclerAdapter::submitList);

        mRecyclerView.setAdapter(recyclerAdapter);
    }

}
