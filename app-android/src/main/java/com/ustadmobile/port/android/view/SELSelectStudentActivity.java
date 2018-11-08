package com.ustadmobile.port.android.view;

import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.SELSelectStudentPresenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.SELSelectStudentView;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Objects;

/**
 * The activity that shows the list of students in this clazz who will be doing the SEL nominations
 */
public class SELSelectStudentActivity extends UstadBaseActivity implements SELSelectStudentView
{

    private Toolbar toolbar;

    //RecyclerView
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mRecyclerLayoutManager;
    private SELSelectStudentPresenter mPresenter;

    private Snackbar studentDoneSnackBar;

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
        SimplePeopleListRecyclerAdapter recyclerAdapter = new SimplePeopleListRecyclerAdapter(
                DIFF_CALLBACK, getApplicationContext(), mPresenter);

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
    public void showStudentDoneMoveOn() {
        studentDoneSnackBar.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //showStudentDoneMoveOn();
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
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        //Setting layout:
        setContentView(R.layout.activity_sel_select_student);

        //Toolbar:
        toolbar = findViewById(R.id.activity_sel_select_student_toolbar);
        toolbar.setTitle(getText(R.string.social_nomination));
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        ConstraintLayout cl = findViewById(R.id.activity_sel_select_student_cl);

        studentDoneSnackBar = Snackbar
                .make(cl, getText(R.string.sel_done_select_another_student), Snackbar.LENGTH_LONG);


        //Recycler View:
        mRecyclerView = findViewById(
                R.id.activity_sel_select_student_recyclerview);
        mRecyclerLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);

        //Call the Presenter
        mPresenter = new SELSelectStudentPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        if (getIntent().hasExtra(ARG_STUDENT_DONE)){
            studentDoneSnackBar.show();
        }

    }


}
