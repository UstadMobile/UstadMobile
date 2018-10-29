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
import com.ustadmobile.core.controller.ClazzDetailEnrollStudentPresenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.ClazzDetailEnrollStudentView;
import com.ustadmobile.lib.db.entities.PersonWithEnrollment;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

import static com.ustadmobile.core.controller.ClazzListPresenter.ARG_CLAZZ_UID;

/**
 * Clazz detail Enroll Student - Enrollment activity.
 * Gets called when "Add Student" is clicked on both the current Clazz selected as well as
 * from the People Bottom Navigation.
 */
public class ClazzDetailEnrollStudentActivity extends UstadBaseActivity implements
        ClazzDetailEnrollStudentView {

    //Toolbar
    private Toolbar toolbar;

    //Recycler View
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mRecyclerLayoutManager;

    //Presenter
    private ClazzDetailEnrollStudentPresenter mPresenter;

    private long currentClazzUid;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set Layout
        setContentView(R.layout.activity_clazz_detail_enroll_student);

        //Toolbar:
        toolbar = findViewById(R.id.activity_clazz_detail_enroll_student_toolbar);
        toolbar.setTitle(getText(R.string.add_student));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Get the clazz Uid from the arguments
        if(getIntent().hasExtra(ARG_CLAZZ_UID)){
            currentClazzUid = getIntent().getLongExtra(ARG_CLAZZ_UID, -1L);
        }

        //RecyclerView:
        mRecyclerView = findViewById(
                R.id.activity_clazz_detail_enroll_student_recycler_view);
        mRecyclerLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);

        //Presenter
        mPresenter = new ClazzDetailEnrollStudentPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        //Enroll new student
        Button newStudentButton = findViewById(R.id.activity_clazz_Detail_enroll_student_new);
        newStudentButton.setOnClickListener(v -> mPresenter.handleClickEnrollNewStudent());

        //FAB
        FloatingTextButton fab = findViewById(R.id.activity_clazz_detail_enroll_student_fab_done);
        fab.setOnClickListener(v -> mPresenter.handleClickDone());

        //TODO:
        //Filter / Sort
        //findViewById(R.id.activity_clazz_detail_enroll_student_filter)
        //        .setOnClickListener(mPresenter.handleChangeSortOrder(...));

        //Search:
        //findViewById(R.id.activity_clazz_detail_enroll_student_search)
        //        .setOnClickListener(mPresenter.handleClickSearch(...););

    }

    @Override
    public void setStudentsProvider(UmProvider<PersonWithEnrollment> studentsProvider) {

        PersonWithEnrollmentRecyclerAdapter recyclerAdapter =
                new PersonWithEnrollmentRecyclerAdapter(DIFF_CALLBACK, getApplicationContext(),
                        this, mPresenter, true, true);

        DataSource.Factory<Integer, PersonWithEnrollment> factory =
                (DataSource.Factory<Integer, PersonWithEnrollment>)
                        studentsProvider.getProvider();

        LiveData<PagedList<PersonWithEnrollment>> data =
                new LivePagedListBuilder<>(factory, 20).build();
        data.observe(this, recyclerAdapter::submitList);

        mRecyclerView.setAdapter(recyclerAdapter);
    }


    // Diff callback.
    public static final DiffUtil.ItemCallback<PersonWithEnrollment> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<PersonWithEnrollment>() {
                @Override
                public boolean areItemsTheSame(PersonWithEnrollment oldItem,
                                               PersonWithEnrollment newItem) {
                    return oldItem.getPersonUid() ==
                            newItem.getPersonUid();
                }

                @Override
                public boolean areContentsTheSame(PersonWithEnrollment oldItem,
                                                  PersonWithEnrollment newItem) {
                    return oldItem.equals(newItem);
                }
            };



}
