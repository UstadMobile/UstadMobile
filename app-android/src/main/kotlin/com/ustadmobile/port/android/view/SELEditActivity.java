package com.ustadmobile.port.android.view;

import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.os.Bundle;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.SELEditPresenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.SELEditView;
import com.ustadmobile.lib.db.entities.PersonWithPersonPicture;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Objects;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;


/**
 * The SELEdit activity - This Activity extends UstadBaseActivity and implements SELEditView -
 * Is responsible for the actual SEL Edit/Detail activity ie: a nomination - with student blobs and selections
 * in them. Is usually linked to a question and run through.
 */
public class SELEditActivity extends UstadBaseActivity implements SELEditView {

    private Toolbar toolbar;
    private RecyclerView mRecyclerView;
    private SELEditPresenter mPresenter;

    /**
     * The DIFF Callback
     */
    public static final DiffUtil.ItemCallback<PersonWithPersonPicture> DIFF_CALLBACK =
        new DiffUtil.ItemCallback<PersonWithPersonPicture>() {
            @Override
            public boolean areItemsTheSame(PersonWithPersonPicture oldItem,
                                           PersonWithPersonPicture newItem) {
                return oldItem == newItem;
            }

            @Override
            public boolean areContentsTheSame(PersonWithPersonPicture oldItem,
                                              PersonWithPersonPicture newItem) {
                return oldItem.equals(newItem);
            }
        };

    @Override
    public void setListProvider(UmProvider<PersonWithPersonPicture> listProvider) {

        // Specify the mAdapter
        PeopleBlobListRecyclerAdapter recyclerAdapter =
            new PeopleBlobListRecyclerAdapter(DIFF_CALLBACK, getApplicationContext(), mPresenter);

        // get the provider, set , observe, etc.
        // A warning is expected
        DataSource.Factory<Integer, PersonWithPersonPicture> factory =
                (DataSource.Factory<Integer, PersonWithPersonPicture>)
                        listProvider.getProvider();
        LiveData<PagedList<PersonWithPersonPicture>> data =
                new LivePagedListBuilder<>(factory, 20).build();
        //Observe the data:
        data.observe(this, recyclerAdapter::submitList);

        //set the adapter
        mRecyclerView.setAdapter(recyclerAdapter);
    }

    /**
     * Updates the heading of the title in this SEL Edit Activity
     *
     * @param questionText  The question text to be put in the heading
     */
    @Override
    public void updateHeading(String questionText) {
        TextView title = findViewById(R.id.activity_sel_edit_title);
        title.setText(questionText);
    }

    /**
     * Updates the toolbar with question number text
     *
     * @param iNum  The current number'th of question
     * @param tNum  The total number of questions in the current SEL run through
     */
    @Override
    public void updateHeading(String iNum, String tNum) {
        toolbar.setTitle(toolbar.getTitle().toString() + " " + iNum + "/" + tNum);
    }

    /**
     * For the back button on toolbar. Its item selected listener.
     *
     * @param item  The MenuItem item
     * @return  true if pressed, false if not.
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

    /**
     * In Order:
     *      1. Sets layout
     *      2. Sets toolbar
     *      3. Sets recycler view - puts a grid layout because of
     *      4. Calls the presenter and its onCreate
     *      5. Sets the floating action button and its listener
     *
     * @param savedInstanceState    The saved instance state
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setting layout:
        setContentView(R.layout.activity_sel_edit);

        //Toolbar:
        toolbar = findViewById(R.id.activity_sel_edit_toolbar);
        toolbar.setTitle(getText(R.string.social_nomination));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //Recycler View:
        mRecyclerView = findViewById(
                R.id.activity_sel_edit_recyclerview);
        RecyclerView.LayoutManager mRecyclerLayoutManager =
                new GridLayoutManager(getApplicationContext(), 3);
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);

        //Call the Presenter
        mPresenter = new SELEditPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        //FAB and its listener
        FloatingTextButton fab = findViewById(R.id.activity_sel_edit_fab);
        fab.setOnClickListener(v -> mPresenter.handleClickPrimaryActionButton());


    }


}
