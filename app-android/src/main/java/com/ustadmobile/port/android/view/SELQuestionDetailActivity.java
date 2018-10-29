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
import android.view.MenuItem;
import android.widget.Button;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.SELQuestionDetailPresenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.SELQuestionDetailView;
import com.ustadmobile.lib.db.entities.SocialNominationQuestion;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;


/**
 * The SELQuestionDetail activity.
 * <p>
 * This Activity extends UstadBaseActivity and implements SELQuestionDetailView
 */
public class SELQuestionDetailActivity extends UstadBaseActivity implements SELQuestionDetailView {

    private Toolbar toolbar;

    //RecyclerView
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mRecyclerLayoutManager;
    private SELQuestionDetailPresenter mPresenter;

    public static final DiffUtil.ItemCallback<SocialNominationQuestion> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<SocialNominationQuestion>() {
                @Override
                public boolean areItemsTheSame(SocialNominationQuestion oldItem,
                                               SocialNominationQuestion newItem) {
                    return oldItem == newItem;
                }

                @Override
                public boolean areContentsTheSame(SocialNominationQuestion oldItem,
                                                  SocialNominationQuestion newItem) {
                    return oldItem.equals(newItem);
                }
            };

    @Override
    public void setListProvider(UmProvider<SocialNominationQuestion> listProvider) {

        // Specify the mAdapter
        SocialNominationQuestionRecyclerAdapter recyclerAdapter =
                new SocialNominationQuestionRecyclerAdapter(DIFF_CALLBACK, getApplicationContext());

        // get the provider, set , observe, etc.
        DataSource.Factory<Integer, SocialNominationQuestion> factory =
                (DataSource.Factory<Integer, SocialNominationQuestion>)
                        listProvider.getProvider();
        LiveData<PagedList<SocialNominationQuestion>> data =
                new LivePagedListBuilder<>(factory, 20).build();
        //Observe the data:
        data.observe(this, recyclerAdapter::submitList);

        //set the adapter
        mRecyclerView.setAdapter(recyclerAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setting layout:
        setContentView(R.layout.activity_sel_question_detail);

        //Toolbar:
        toolbar = findViewById(R.id.activity_sel_question_detail_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Recycler View:
        mRecyclerView = findViewById(
                R.id.activity_sel_question_detail_recyclerview);
        mRecyclerLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);

        //Call the Presenter
        mPresenter = new SELQuestionDetailPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        //FAB and its listener
        FloatingTextButton fab = findViewById(R.id.activity_sel_question_detail_fab);
        fab.setOnClickListener(v -> mPresenter.handleClickDone());

        //Add question button
        Button addQuestionButton =
                findViewById(R.id.activity_sel_question_detail_add_question_button);
        addQuestionButton.setOnClickListener(v -> mPresenter.handleClickAddQuestion());

    }


}
