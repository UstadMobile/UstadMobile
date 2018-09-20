package com.ustadmobile.port.android.view;

import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.arch.paging.PagedListAdapter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ClazzListEnrollPersonPresenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.ClazzListEnrollPersonView;
import com.ustadmobile.lib.db.entities.ClazzWithEnrollment;
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import static com.ustadmobile.core.view.PersonDetailView.ARG_PERSON_UID;

public class ClazzListEnrollPersonActivity extends UstadBaseActivity implements ClazzListEnrollPersonView {

    private Toolbar toolbar;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mRecyclerLayoutManager;

    //TODO: check this
    private ClazzListEnrollPersonPresenter mPresenter;

    private long personUid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set layout
        setContentView(R.layout.activity_clazz_list_enroll_person);

        //Toolbar
        toolbar = findViewById(R.id.activity_clazz_list_enroll_person_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Get the argument person to enroll the classes to / get enrolled classes
        if(getIntent().hasExtra(ARG_PERSON_UID)){
            personUid = getIntent().getLongExtra(ARG_PERSON_UID, -1L);
        }

        mRecyclerView = (RecyclerView) findViewById(
                R.id.activity_clazz_list_enroll_person_recyclerview);
        mRecyclerLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);
        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(mRecyclerView.getContext(),
                        LinearLayoutManager.VERTICAL);

        //Call the presenter
        mPresenter = new ClazzListEnrollPersonPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);

        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        //FAB
        findViewById(R.id.activity_clazz_list_enroll_person_fab)
                .setOnClickListener(v -> mPresenter.handleClickDone());

    }

    @Override
    public void setClazzListProvider(UmProvider<ClazzWithEnrollment> clazzListProvider) {

        ClazzListEnrollPersonRecyclerAdapter recyclerAdapter =
                new ClazzListEnrollPersonRecyclerAdapter(DIFF_CALLBACK);
        DataSource.Factory<Integer, ClazzWithEnrollment> factory =
                (DataSource.Factory<Integer, ClazzWithEnrollment>)clazzListProvider.getProvider();
        LiveData<PagedList<ClazzWithEnrollment>> data =
                new LivePagedListBuilder<>(factory, 20).build();
        data.observe(this, recyclerAdapter::submitList);

        mRecyclerView.setAdapter(recyclerAdapter);
    }


    /**
     * The ClazzList Recycler Adapter used here.
     */
    protected class ClazzListEnrollPersonRecyclerAdapter extends
            PagedListAdapter<ClazzWithEnrollment,
                    ClazzListEnrollPersonRecyclerAdapter.ClazzViewHolder> {

        protected class ClazzViewHolder extends RecyclerView.ViewHolder {

            protected ClazzViewHolder(View itemView) {
                super(itemView);
            }
        }

        protected ClazzListEnrollPersonRecyclerAdapter(
                @NonNull DiffUtil.ItemCallback<ClazzWithEnrollment>
                        diffCallback) {
            super(diffCallback);
        }

        @NonNull
        @Override
        public ClazzViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View clazzListItem =
                    LayoutInflater.from(getApplicationContext())
                            .inflate(R.layout.item_clazz_list_enroll_person, parent, false);
            return new ClazzViewHolder(clazzListItem);
        }

        /**
         * This method sets the elements after it has been obtained for that item'th position.
         *
         * @param holder The view holder
         * @param position The position of the item
         */
        @Override
        public void onBindViewHolder  (@NonNull ClazzViewHolder holder, int position) {
            ClazzWithEnrollment clazz = getItem(position);
            ((TextView)holder.itemView.findViewById(R.id.item_clazz_list_enroll_person_title))
                    .setText(clazz.getClazzName());
            ((TextView)holder.itemView.findViewById(R.id.item_clazz_list_enroll_person_numstudents_text))
                    .setText(clazz.getNumStudents() + " " + getResources()
                            .getText(R.string.students_literal).toString());
            ((Switch)holder.itemView.findViewById(R.id.item_clazz_list_enroll_person_switch))
                    .setChecked(clazz.getEnrolled());
            holder.itemView.setOnClickListener((view) -> mPresenter.handleClickClazz(clazz));

        }
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
}
