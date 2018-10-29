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
import android.widget.CheckBox;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.SELRecognitionPresenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.SELRecognitionView;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Objects;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;


/**
 * The SELRecognition activity.
 * <p>
 * This Activity extends UstadBaseActivity and implements SELRecognitionView
 */
public class SELRecognitionActivity extends UstadBaseActivity implements SELRecognitionView {

    private Toolbar toolbar;

    //RecyclerView
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mRecyclerLayoutManager;
    private SELRecognitionPresenter mPresenter;

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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setListProvider(UmProvider<Person> listProvider) {

        // Specify the mAdapter
        PeopleBlobListRecyclerAdapter recyclerAdapter =
                new PeopleBlobListRecyclerAdapter(DIFF_CALLBACK, getApplicationContext(),
                        mPresenter, true);

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
        setContentView(R.layout.activity_sel_recognition);

        //Toolbar:
        toolbar = findViewById(R.id.activity_sel_recognition_toolbar);
        toolbar.setTitle(getText(R.string.social_nomination));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);


        //Recycler View:
        mRecyclerView = findViewById(
                R.id.activity_sel_recognition_recyclerview);
        //mRecyclerLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerLayoutManager = new GridLayoutManager(getApplicationContext(), 3);
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);

        //Call the Presenter
        mPresenter = new SELRecognitionPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        CheckBox recognizedCheckBox = findViewById(R.id.activity_sel_recognition_checkbox);

        //FAB and its listener
        FloatingTextButton fab = findViewById(R.id.activity_sel_recognition_fab);
        fab.setOnClickListener(v ->
                mPresenter.handleClickPrimaryActionButton(recognizedCheckBox.isChecked()));


    }


}
