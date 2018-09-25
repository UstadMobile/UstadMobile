package com.ustadmobile.port.android.view;

import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.arch.paging.PagedListAdapter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ClazzDetailEnrollStudentPresenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.ClazzDetailEnrollStudentView;
import com.ustadmobile.lib.db.entities.PersonWithEnrollment;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

import static com.ustadmobile.core.controller.ClazzListPresenter.ARG_CLAZZ_UID;

public class ClazzDetailEnrollStudentActivity extends UstadBaseActivity implements
        ClazzDetailEnrollStudentView {


    //Toolbar
    private Toolbar toolbar;

    //Recycler View
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mRecyclerLayoutManager;

    private ClazzDetailEnrollStudentPresenter mPresenter;

    private long currentClazzUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set Layout
        setContentView(R.layout.activity_clazz_detail_enroll_student);

        //Toolbar:
        toolbar = findViewById(R.id.activity_clazz_detail_enroll_student_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Get the clazz Uid from thearguments
        if(getIntent().hasExtra(ARG_CLAZZ_UID)){
            currentClazzUid = getIntent().getLongExtra(ARG_CLAZZ_UID, -1L);
        }

        //RecyclerView:
        mRecyclerView = (RecyclerView) findViewById(R.id. activity_clazz_detail_enroll_student_recycler_view);
        mRecyclerLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);
        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(mRecyclerView.getContext(), LinearLayout.VERTICAL);

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

        //Filter / Sort
        //findViewById(R.id.activity_clazz_detail_enroll_student_filter)
        //        .setOnClickListener(mPresenter.handleChangeSortOrder(...));

        //Search:
        //findViewById(R.id.activity_clazz_detail_enroll_student_search)
        //        .setOnClickListener(mPresenter.handleClickSearch(...););

    }

    @Override
    public void setStudentsProvider(UmProvider<PersonWithEnrollment> studentsProvider) {
        ClazzDetailEnrollStudentRecyclerAdapter recyclerAdapter =
                new ClazzDetailEnrollStudentRecyclerAdapter(DIFF_CALLBACK);

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


    protected class ClazzDetailEnrollStudentRecyclerAdapter
            extends PagedListAdapter<PersonWithEnrollment,
                        ClazzDetailEnrollStudentRecyclerAdapter.ClazzLogDetailViewHolder> {

        protected class ClazzLogDetailViewHolder extends RecyclerView.ViewHolder{
            protected ClazzLogDetailViewHolder(View itemView){
                super(itemView);
            }
        }

        protected ClazzDetailEnrollStudentRecyclerAdapter(
                @NonNull DiffUtil.ItemCallback<PersonWithEnrollment> diffCallback){
            super(diffCallback);
        }

        @NonNull
        @Override
        public ClazzLogDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){

            View clazzLogDetailListItem =
                    LayoutInflater.from(getApplicationContext()).inflate(
                            R.layout.item_studentlistenroll_student, parent, false);
            return new ClazzLogDetailViewHolder(clazzLogDetailListItem);
        }

        /**
         * This method sets the elements after it has been obtained for that item'th position.
         *
         * Every item in the recycler view will have set its colors if no attendance status is set.
         * every attendance button will have it-self mapped to tints on activation.
         *
         * @param holder
         * @param position
         */
        @Override
        public void onBindViewHolder(@NonNull ClazzLogDetailViewHolder holder, int position){
            PersonWithEnrollment personWithEnrollment = getItem(position);

            String studentName = personWithEnrollment.getFirstNames() + " " +
                    personWithEnrollment.getLastName();

            long attendancePercentage =
                    (long) (personWithEnrollment.getAttendancePercentage() * 100);
            String studentAttendancePercentage = attendancePercentage +
                    "% " + getText(R.string.attendance);
            ImageView trafficLight = ((ImageView) holder.itemView
                    .findViewById(R.id.item_studentlist_student_simple_attendance_trafficlight));
            if(attendancePercentage > 75L){
                trafficLight.setColorFilter(ContextCompat.getColor(getApplicationContext(),
                        R.color.traffic_green));
            }else if(attendancePercentage > 50L){
                trafficLight.setColorFilter(ContextCompat.getColor(getApplicationContext(),
                        R.color.traffic_orange));
            }else{
                trafficLight.setColorFilter(ContextCompat.getColor(getApplicationContext(),
                        R.color.traffic_red));
            }

            ((TextView)holder.itemView
                    .findViewById(R.id.item_studentlist_student_simple_student_title))
                    .setText(studentName);
            ((TextView)holder.itemView
                    .findViewById(R.id.item_studentlist_student_simple_attendance_percentage))
                    .setText(studentAttendancePercentage);

            holder.itemView.findViewById(R.id.item_studentlist_student_simple_student_checkbox).setVisibility(View.VISIBLE);
            ((CheckBox)holder.itemView.findViewById(R.id.item_studentlist_student_simple_student_checkbox))
                    .setChecked(personWithEnrollment.getEnrolled());
            ((CheckBox)holder.itemView.findViewById(R.id.item_studentlist_student_simple_student_checkbox))
                    .setOnCheckedChangeListener((buttonView, isChecked) ->
                            mPresenter.handleEnrollChanged(personWithEnrollment, isChecked));



        }
    }
}
