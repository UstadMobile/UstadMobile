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
import com.ustadmobile.lib.db.entities.SelQuestion;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Objects;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;


/**
 * The SELQuestionDetail activity.This Activity extends UstadBaseActivity and implements
 * SELQuestionDetailView. This activity is responsible for displaying all questions for SEL
 * and UI component to add more questions.
 */
public class SELQuestionDetailActivity extends UstadBaseActivity implements SELQuestionDetailView {

    private RecyclerView mRecyclerView;
    private SELQuestionDetailPresenter mPresenter;

    /**
     * The DIFF CALLBACK
     */
    public static final DiffUtil.ItemCallback<SelQuestion> DIFF_CALLBACK =
        new DiffUtil.ItemCallback<SelQuestion>() {
            @Override
            public boolean areItemsTheSame(SelQuestion oldItem,
                                           SelQuestion newItem) {
                return oldItem == newItem;
            }

            @Override
            public boolean areContentsTheSame(SelQuestion oldItem,
                                              SelQuestion newItem) {
                return oldItem.equals(newItem);
            }
        };

    /**
     * Sets questions list provider to the recycler adapter.
     *
     * @param listProvider The provider data
     */
    @Override
    public void setListProvider(UmProvider<SelQuestion> listProvider) {

        // Specify the mAdapter
        SocialNominationQuestionRecyclerAdapter recyclerAdapter =
                new SocialNominationQuestionRecyclerAdapter(DIFF_CALLBACK, getApplicationContext());

        // get the provider, set , observe, etc.
        // A warning is expected
        DataSource.Factory<Integer, SelQuestion> factory =
                (DataSource.Factory<Integer, SelQuestion>)
                        listProvider.getProvider();
        LiveData<PagedList<SelQuestion>> data =
                new LivePagedListBuilder<>(factory, 20).build();
        //Observe the data:
        data.observe(this, recyclerAdapter::submitList);

        //set the adapter
        mRecyclerView.setAdapter(recyclerAdapter);
    }

    /**
     This method catches menu buttons/options pressed in the toolbar. Here it is making sure
     * the activity goes back when the back button is pressed.
     *
     * @param item  The item selected
     * @return  true if accounted for
     */
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
        Toolbar toolbar = findViewById(R.id.activity_sel_question_detail_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //Recycler View:
        mRecyclerView = findViewById(
                R.id.activity_sel_question_detail_recyclerview);
        RecyclerView.LayoutManager mRecyclerLayoutManager = new LinearLayoutManager(getApplicationContext());
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
