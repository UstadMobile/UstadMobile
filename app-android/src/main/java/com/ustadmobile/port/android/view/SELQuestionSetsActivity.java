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
import com.ustadmobile.core.controller.SELQuestionPresenter;
import com.ustadmobile.core.controller.SELQuestionSetsPresenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.SELQuestionSetsView;
import com.ustadmobile.lib.db.entities.SELQuestionSetWithNumQuestions;
import com.ustadmobile.lib.db.entities.SocialNominationQuestionSet;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Objects;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

public class SELQuestionSetsActivity extends UstadBaseActivity implements SELQuestionSetsView {

    private Toolbar toolbar;
    private SELQuestionSetsPresenter mPresenter;
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
        setContentView(R.layout.activity_sel_question_sets);

        //Toolbar:
        toolbar = findViewById(R.id.activity_sel_question_sets_toolbar);
        toolbar.setTitle(getText(R.string.sel_question_set));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //RecyclerView
        mRecyclerView = findViewById(
                R.id.activity_sel_question_sets_recyclerview);
        RecyclerView.LayoutManager mRecyclerLayoutManager =
                new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);

        //Call the Presenter
        mPresenter = new SELQuestionSetsPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        //FAB and its listener
        FloatingTextButton fab = findViewById(R.id.activity_sel_question_sets_fab);
        fab.setOnClickListener(v -> mPresenter.handleClickPrimaryActionButton());

    }

    /**
     * The DIFF CALLBACK
     */
    public static final DiffUtil.ItemCallback<SELQuestionSetWithNumQuestions> DIFF_CALLBACK =
        new DiffUtil.ItemCallback<SELQuestionSetWithNumQuestions>() {
            @Override
            public boolean areItemsTheSame(SELQuestionSetWithNumQuestions oldItem,
                                           SELQuestionSetWithNumQuestions newItem) {
                return oldItem == newItem;
            }

            @Override
            public boolean areContentsTheSame(SELQuestionSetWithNumQuestions oldItem,
                                              SELQuestionSetWithNumQuestions newItem) {
                return oldItem.equals(newItem);
            }
        };

    @Override
    public void setListProvider(UmProvider<SELQuestionSetWithNumQuestions> listProvider) {
        SELQuestionSetListRecyclerAdapter recyclerAdapter =
                new SELQuestionSetListRecyclerAdapter(DIFF_CALLBACK, mPresenter,
                        getApplicationContext());

        // get the provider, set , observe, etc.
        // A warning is expected
        DataSource.Factory<Integer, SELQuestionSetWithNumQuestions> factory =
                (DataSource.Factory<Integer, SELQuestionSetWithNumQuestions>)
                        listProvider.getProvider();
        LiveData<PagedList<SELQuestionSetWithNumQuestions>> data =
                new LivePagedListBuilder<>(factory, 20).build();
        //Observe the data:
        data.observe(this, recyclerAdapter::submitList);

        //set the adapter
        mRecyclerView.setAdapter(recyclerAdapter);
    }
}
