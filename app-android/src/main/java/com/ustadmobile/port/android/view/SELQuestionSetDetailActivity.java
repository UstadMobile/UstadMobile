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

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.SELQuestionSetDetailPresenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.SELQuestionSetDetailView;
import com.ustadmobile.lib.db.entities.SocialNominationQuestion;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Objects;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

public class SELQuestionSetDetailActivity extends
        UstadBaseActivity implements SELQuestionSetDetailView {

    private Toolbar toolbar;
    private SELQuestionSetDetailPresenter mPresenter;
    private RecyclerView mRecyclerView;


    /**
     * This method catches menu buttons/options pressed in the toolbar. Here it is making sure
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
        setContentView(R.layout.activity_sel_question_set_detail);

        //Toolbar:
        toolbar = findViewById(R.id.activity_sel_question_set_detail_toolbar);
        toolbar.setTitle(getText(R.string.sel_question_set));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //RecyclerView
        mRecyclerView = findViewById(
                R.id.activity_sel_question_set_detail_recyclerview);
        RecyclerView.LayoutManager mRecyclerLayoutManager =
                new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);

        //Call the Presenter
        mPresenter = new SELQuestionSetDetailPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        //FAB and its listener
        FloatingTextButton fab = findViewById(R.id.activity_sel_question_set_detail_fab);
        fab.setOnClickListener(v -> mPresenter.handleClickPrimaryActionButton());

    }

    /**
     * The DIFF CALLBACK
     */
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

        SELQuestionRecyclerAdapter recyclerAdapter =
                new SELQuestionRecyclerAdapter(DIFF_CALLBACK, getApplicationContext(),
                        this, mPresenter);

        // get the provider, set , observe, etc.
        // A warning is expected
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
    public void updateToolbarTitle(String title) {
        toolbar.setTitle(title);
    }
}
