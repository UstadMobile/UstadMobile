package com.ustadmobile.port.android.view;

import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.os.Bundle;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ClazzStudentListPresenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.ClazzStudentListView;
import com.ustadmobile.lib.db.entities.PersonWithEnrollment;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

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


    public static final DiffUtil.ItemCallback<PersonWithEnrollment> DIFF_CALLBACK2 =
            new DiffUtil.ItemCallback<PersonWithEnrollment>() {
                @Override
                public boolean areItemsTheSame(PersonWithEnrollment oldItem,
                                               PersonWithEnrollment newItem) {
                    return oldItem.getPersonUid() == newItem.getPersonUid();
                }

                @Override
                public boolean areContentsTheSame(PersonWithEnrollment oldItem,
                                                  PersonWithEnrollment newItem) {
                    return oldItem.equals(newItem);
                }
            };
    /**
     * Generates a new Fragment for a page fragment
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
        mPresenter = new ClazzStudentListPresenter(getContext(),
                UMAndroidUtil.bundleToHashtable(getArguments()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        //FAB
        FloatingTextButton fab = rootContainer.findViewById(R.id.class_student_list_add_student_fab);
        fab.setOnClickListener(v -> mPresenter.goToAddStudentFragment());

        return rootContainer;
    }

    @Override
    public void setPersonWithEnrollmentProvider(
            UmProvider<PersonWithEnrollment> setPersonUmProvider) {

        PersonWithEnrollmentRecyclerAdapter recyclerAdapter =
                new PersonWithEnrollmentRecyclerAdapter(DIFF_CALLBACK2, getContext(),
                        this, mPresenter, true, false);


        DataSource.Factory<Integer, PersonWithEnrollment> factory =
                (DataSource.Factory<Integer, PersonWithEnrollment>)
                        setPersonUmProvider.getProvider();
        LiveData<PagedList<PersonWithEnrollment>> data =
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
