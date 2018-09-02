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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ClazzStudentListPresenter;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.ClazzStudentListView;
import com.ustadmobile.lib.db.entities.ClazzMember;
import com.ustadmobile.lib.db.entities.ClazzMemberWithPerson;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import static com.ustadmobile.core.controller.ClazzListPresenter.ARG_CLAZZ_UID;

/**
 * ClazzStudentListFragment Android fragment extends UstadBaseFragment
 */
public class ClazzStudentListFragment extends UstadBaseFragment implements ClazzStudentListView,
        View.OnClickListener, View.OnLongClickListener {

    View rootContainer;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mRecyclerLayoutManager;
    private ClazzStudentListPresenter mPresenter;

    public long clazzUid;


    /**
     * The Recycler Adapter for Student list.
     */
    protected class ClazzStudentListRecyclerAdapter extends
            PagedListAdapter<ClazzMemberWithPerson,
                    ClazzStudentListRecyclerAdapter.ClazzStudentViewHolder> {

        protected class ClazzStudentViewHolder extends RecyclerView.ViewHolder {
            protected ClazzStudentViewHolder(View itemView) {
                super(itemView);
            }
        }

        protected ClazzStudentListRecyclerAdapter(@NonNull DiffUtil.ItemCallback<ClazzMemberWithPerson> diffCallback) {
            super(diffCallback);
        }

        /**
         * This method inflates the card layout (to parent view given) and returns it.
         * @param parent View given.
         * @param viewType View Type not used here.
         * @return New ViewHolder for the ClazzStudent type
         */
        @NonNull
        @Override
        public ClazzStudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View clazzStudentListItem =
                    LayoutInflater.from(getContext()).inflate(
                            R.layout.item_clazzstudentlist_student, parent, false);
            return new ClazzStudentViewHolder(clazzStudentListItem);
        }

        /**
         * This method sets the elements after it has been obtained for that item'th position.
         * @param holder    The holder
         * @param position  The position in the recycler view.
         */
        @Override
        public void onBindViewHolder(@NonNull ClazzStudentViewHolder holder, int position) {
            ClazzMemberWithPerson clazzMemberWithPerson = getItem(position);
            String studentName;
            if(clazzMemberWithPerson.getPerson() == null){
                studentName = "Null Student";
            }else{
                studentName = clazzMemberWithPerson.getPerson().getFirstName() + " " +
                        clazzMemberWithPerson.getPerson().getLastName();
            }

            long attendancePercentage =
                    (long) (clazzMemberWithPerson.getAttendancePercentage() * 100);

            String studentAttendancePercentage = attendancePercentage +
                    "% " + getText(R.string.attendance);
            ImageView trafficLight = ((ImageView) holder.itemView
                    .findViewById(R.id.item_clazzstudentlist_attendance_trafficlight));
            if(attendancePercentage > 75L){
                trafficLight.setColorFilter(ContextCompat.getColor(getContext(), R.color.traffic_green));
            }else if(attendancePercentage > 50L){
                trafficLight.setColorFilter(ContextCompat.getColor(getContext(), R.color.traffic_orange));
            }else{
                trafficLight.setColorFilter(ContextCompat.getColor(getContext(), R.color.traffic_red));
            }

            ((TextView)holder.itemView
                .findViewById(R.id.item_clazzstudentlist_student_title))
                .setText(studentName);
            ((TextView)holder.itemView
                .findViewById(R.id.item_clazzstudentlist_student_attendance_percentage))
                .setText(studentAttendancePercentage);

        }
    }

    public static final DiffUtil.ItemCallback<ClazzMemberWithPerson> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<ClazzMemberWithPerson>() {
                @Override
                public boolean areItemsTheSame(ClazzMemberWithPerson oldItem,
                                               ClazzMemberWithPerson newItem) {
                    return oldItem.getClazzMemberUid() == newItem.getClazzMemberUid();
                }

                @Override
                public boolean areContentsTheSame(ClazzMemberWithPerson oldItem,
                                                  ClazzMemberWithPerson newItem) {
                    return oldItem.equals(newItem);
                }
            };

    /**
     * Generates a new Fragment for a page fragment
     * TODO: Add any args if needed
     *
     * @return A new instance of fragment ClazzStudentListFragment.
     */
    public static ClazzStudentListFragment newInstance(long clazzUid) {
        ClazzStudentListFragment fragment = new ClazzStudentListFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_CLAZZ_UID, clazzUid);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * On Create of the fragment.
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * On Create of the View fragment . Part of Android's Fragment Override
     *
     * This method will get run every time the View is created.
     *
     * This method readies the recycler view and its layout
     * This method sets the presenter and calls its onCreate
     * That then populates the recycler view.
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return the root container
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootContainer =
                inflater.inflate(R.layout.fragment_class_student_list, container, false);
        setHasOptionsMenu(true);

        mRecyclerView = rootContainer.findViewById(R.id.fragment_class_student_list_recyclerview);
        mRecyclerLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);
        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(mRecyclerView.getContext(), LinearLayoutManager.VERTICAL);

        //Create the presenter and call its onCreate method. This will populate the provider data
        // and call setProvider to set it
        mPresenter = new ClazzStudentListPresenter(this,
                UMAndroidUtil.bundleToHashtable(getArguments()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        return rootContainer;
    }

    /**
     * Creates the recycler adapter and sets it to the recycler view. Called by
     * ClazzStudentListPresenter's onCreate()
     *
     * @param clazzMembersUmProvider Of UmProvider typed with the kind of data
     */
    @Override
    public void setClazzMembersProvider(
            UmProvider<ClazzMemberWithPerson> clazzMembersUmProvider) {
        //Create a recycler adapter to set on the RecyclerView.
        // The DIFF_CALLBACK makes sure it updated only if data actually changed.
        ClazzStudentListRecyclerAdapter recyclerAdapter =
                new ClazzStudentListRecyclerAdapter(DIFF_CALLBACK);

        DataSource.Factory<Integer, ClazzMemberWithPerson> factory =
                (DataSource.Factory<Integer, ClazzMemberWithPerson>)
                        clazzMembersUmProvider.getProvider();
        LiveData<PagedList<ClazzMemberWithPerson>> data =
                new LivePagedListBuilder<>(factory, 20).build();
        data.observe(this, recyclerAdapter::submitList);

        mRecyclerView.setAdapter(recyclerAdapter);

    }

    // This event is triggered soon after onCreateView().
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Setup any handles to view objects here

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }


}
