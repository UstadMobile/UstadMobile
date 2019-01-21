package com.ustadmobile.port.android.view;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ClazzStudentListPresenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.ClazzStudentListView;
import com.ustadmobile.lib.db.entities.ClazzMember;
import com.ustadmobile.lib.db.entities.PersonWithEnrollment;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Objects;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

import static com.ustadmobile.core.view.ClazzListView.ARG_CLAZZ_UID;
import static com.ustadmobile.port.android.view.PersonEditActivity.DEFAULT_PADDING;

/**
 * ClazzStudentListFragment Android fragment extends UstadBaseFragment
 */
public class ClazzStudentListFragment extends UstadBaseFragment implements ClazzStudentListView {

    View rootContainer;
    private RecyclerView mRecyclerView;
    private ClazzStudentListPresenter mPresenter;
    private Spinner sortSpinner;
    String[] sortSpinnerPresets;
    private ConstraintLayout cl;
    private boolean addClazzMemberEmptyAdded;

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
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootContainer =
                inflater.inflate(R.layout.fragment_class_student_list, container, false);
        setHasOptionsMenu(true);


        cl = rootContainer.findViewById(R.id.fragment_class_student_list_cl);

        mRecyclerView = rootContainer.findViewById(R.id.fragment_class_student_list_recyclerview);
        RecyclerView.LayoutManager mRecyclerLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);


        //Sort Fragment:
        sortSpinner = rootContainer.findViewById(R.id.fragment_class_student_list_sort_spinner);

        //Reset created flags
        addClazzMemberEmptyAdded = false;

        //Create the presenter and call its onCreate method. This will populate the provider data
        // and call setProvider to set it
        mPresenter = new ClazzStudentListPresenter(getContext(),
                UMAndroidUtil.bundleToHashtable(getArguments()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));


        //Sort handler
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mPresenter.handleChangeSortOrder(id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return rootContainer;
    }

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

    @Override
    public void setPersonWithEnrollmentProvider(
            UmProvider<PersonWithEnrollment> setPersonUmProvider) {

        PersonWithEnrollmentRecyclerAdapter recyclerAdapter =
                new PersonWithEnrollmentRecyclerAdapter(DIFF_CALLBACK2, getContext(),
                        this, mPresenter, true, false);

        recyclerAdapter.setShowAddStudent(mPresenter.isCanAddStudents());
        recyclerAdapter.setShowAddTeacher(mPresenter.isCanAddTeachers());

        //A warning is expected
        DataSource.Factory<Integer, PersonWithEnrollment> factory =
                (DataSource.Factory<Integer, PersonWithEnrollment>)
                        setPersonUmProvider.getProvider();
        LiveData<PagedList<PersonWithEnrollment>> data =
                new LivePagedListBuilder<>(factory, 20).build();

        Observer customObserver = o -> {

            if(((PagedList<PersonWithEnrollment>) o).size() == 0){
                if(!addClazzMemberEmptyAdded) {
                    removeAddClazzMemberHeadings();
                    addAddClazzMemberHeadings();
                    addClazzMemberEmptyAdded = true;
                }
            }else{
                if(addClazzMemberEmptyAdded) {
                    removeAddClazzMemberHeadings();
                    addClazzMemberEmptyAdded = false;
                }
            }
            recyclerAdapter.submitList((PagedList<PersonWithEnrollment>) o);
        };

        data.observe(this, customObserver);
        mRecyclerView.setAdapter(recyclerAdapter);
    }

    /**
     * Removes old Add ClazzMember views
     *
     */
    private void removeAddClazzMemberHeadings(){

        //Get Clazz Member layout for student and teacher
        View addCMCLViewS = rootContainer.findViewById(addCMCLS);
        View addCMCLViewT = rootContainer.findViewById(addCMCLT);

        //Remove the views
        cl.removeView(addCMCLViewS);
        cl.removeView(addCMCLViewT);

        //If view exists, set it to invisible
        if(addCMCLViewS != null){
            addCMCLViewS.setVisibility(View.INVISIBLE);
        }
        if(addCMCLViewT != null){
            addCMCLViewT.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Add new "Add Student/Teacher" headings
     */
    private void addAddClazzMemberHeadings(){
        if(mPresenter.isTeachersEditable()) {
            addHeadingAndNew(cl, ClazzMember.ROLE_TEACHER, getContext());
        }

        addHeadingAndNew(cl, ClazzMember.ROLE_STUDENT, getContext());
    }

    @Override
    public void updateSortSpinner(String[] presets) {
        this.sortSpinnerPresets = presets;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(Objects.requireNonNull(getContext()),
                R.layout.spinner_item, sortSpinnerPresets);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(adapter);
    }


    /**
     * Get dp from pixel
     *
     * @param dp the pixels
     * @return  The dp
     */
    private int getDp(int dp){

        return Math.round(
                dp * getResources().getDisplayMetrics().density);
    }


    private int addCMCLT, addCMCLS;
    Boolean teacherAdded = false;
    private int currentTop = -1;

    /**
     * Adds a heading depending on role given
     * @param cl    The Constraint layout where the list will be in.
     * @param role  The role (Teacher or Student) as per ClazzMember.ROLE_*
     */
    private void addHeadingAndNew(ConstraintLayout cl, int role, Context mContext){

        ConstraintLayout addCl = new ConstraintLayout(mContext);
        int defaultPadding =  getDp(DEFAULT_PADDING);

        int defaultPaddingBy2 = getDp(DEFAULT_PADDING/2);

        TextView clazzMemberRoleHeadingTextView = new TextView(mContext);
        clazzMemberRoleHeadingTextView.setTextColor(Color.BLACK);
        clazzMemberRoleHeadingTextView.setTextSize(16);
        clazzMemberRoleHeadingTextView.setLeft(8);

        int addIconResId = getResources().getIdentifier(PersonEditActivity.ADD_PERSON_ICON,
                "drawable", Objects.requireNonNull(getActivity()).getPackageName());

        ImageView addPersonImageView = new ImageView(mContext);
        addPersonImageView.setImageResource(addIconResId);

        TextView addClazzMemberTextView = new TextView(mContext);
        addClazzMemberTextView.setTextColor(Color.BLACK);
        addClazzMemberTextView.setTextSize(16);
        addClazzMemberTextView.setLeft(8);

        //Horizontal line
        View horizontalLine = new View(mContext);
        horizontalLine.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                getDp(1)
        ));
        horizontalLine.setBackgroundColor(Color.parseColor("#EAEAEA"));

        //Get ids for all components.
        int headingId = View.generateViewId();
        int headingImageId = View.generateViewId();
        int addClazzMemberId = View.generateViewId();
        int hLineId = View.generateViewId();
        int addCMCL = View.generateViewId();


        //Set strings and handler on the components based on role.
        if (role == ClazzMember.ROLE_STUDENT) {
            addClazzMemberTextView.setText(getText(R.string.add_student));
            clazzMemberRoleHeadingTextView.setText(getText(R.string.students_literal));
            addCl.setOnClickListener( v -> mPresenter.handleCommonPressed(0L));

            //Storing in separate variables so we can remove them.
            addCMCLS = addCMCL;

        } else {

            teacherAdded = false;

            addClazzMemberTextView.setText(getText(R.string.add_teacher));
            clazzMemberRoleHeadingTextView.setText(getText(R.string.teachers_literal));
            addCl.setOnClickListener(v -> mPresenter.handleSecondaryPressed(-1L));

            //Storing in separate variables so we can remove them.
            addCMCLT = addCMCL;

            //For Teachers (which will always be the start of the recycler view: we keep the top as
            // the top of the whole inner constraint layout .
            currentTop = addCMCL;
        }

        //Set ids for all components.
        clazzMemberRoleHeadingTextView.setId(headingId);
        addClazzMemberTextView.setId(addClazzMemberId);
        horizontalLine.setId(hLineId);
        addPersonImageView.setId(headingImageId);
        addCl.setId(addCMCL);

        //Add these components to the new "add" Constraint Layout
        addCl.addView(clazzMemberRoleHeadingTextView);
        addCl.addView(addPersonImageView);
        addCl.addView(addClazzMemberTextView);
        addCl.addView(horizontalLine);

        ConstraintSet constraintSetForHeader2 = new ConstraintSet();
        constraintSetForHeader2.clone(addCl);

        //[Teachers / Students Heading] TOP to TOP of whichever the top
        //  (can be Parent or horizontal line of previous item)
        constraintSetForHeader2.connect(
                headingId, ConstraintSet.TOP,
                addCl.getId(), ConstraintSet.TOP,
                defaultPaddingBy2);

        //[Teachers / Students Heading] START to START of PARENT (always)
        constraintSetForHeader2.connect(
                headingId, ConstraintSet.START,
                addCl.getId(), ConstraintSet.START,
                defaultPaddingBy2);

        //[Add teacher/student Icon] START to START of Parent (always)
        constraintSetForHeader2.connect(
                headingImageId, ConstraintSet.START,
                addCl.getId(), ConstraintSet.START, defaultPadding);

        //[Add teacher/student Icon] TOP to BOTTOM of Heading
        constraintSetForHeader2.connect(
                headingImageId, ConstraintSet.TOP,
                headingId, ConstraintSet.BOTTOM, defaultPaddingBy2);

        //[Add teacher/student Text]  START to Icon END (always)
        constraintSetForHeader2.connect(
                addClazzMemberId, ConstraintSet.START,
                headingImageId, ConstraintSet.END, defaultPadding);

        //[Add teacher/student Text] TOP to [Teacher / Students Heading] Bottom (always)
        constraintSetForHeader2.connect(
                addClazzMemberId, ConstraintSet.TOP,
                headingId, ConstraintSet.BOTTOM, defaultPaddingBy2);

        //[Add Teacher/Student HLine] TOP to [Teacher / Student Icon] BOTTOM (always)
        constraintSetForHeader2.connect(
                hLineId, ConstraintSet.TOP,
                headingImageId, ConstraintSet.BOTTOM, defaultPaddingBy2);

        //[Add Teacher/Student HLine] START to Parent (always)
        constraintSetForHeader2.connect(hLineId, ConstraintSet.START,
                addCl.getId(), ConstraintSet.START, 0);

        //Current Person image TOP to BOTTOM of horizontal line (always)
        constraintSetForHeader2.connect(
                R.id.item_studentlist_student_simple_student_image,ConstraintSet.TOP,
                hLineId, ConstraintSet.BOTTOM, defaultPaddingBy2);

        //Current Person title TOP to BOTTOM of horizontal line (always)
        constraintSetForHeader2.connect(
                R.id.item_studentlist_student_simple_student_title, ConstraintSet.TOP,
                hLineId, ConstraintSet.BOTTOM, defaultPaddingBy2);

        constraintSetForHeader2.applyTo(addCl);

        cl.addView(addCl);

        ConstraintSet constraintSetForHeader = new ConstraintSet();
        constraintSetForHeader.clone(cl);


        if(teacherAdded){

            // [Add CL Student ] always on top to top parent.
            constraintSetForHeader.connect(
                    addCl.getId(), ConstraintSet.TOP,
                    currentTop, ConstraintSet.BOTTOM, defaultPaddingBy2);

            // [ Add CL [Teachers] ] always start to start parent.
            constraintSetForHeader.connect(
                    addCl.getId(), ConstraintSet.START,
                    cl.getId(), ConstraintSet.START, defaultPaddingBy2);

        } else {

            // [Add CL Teacher ] always on top to top parent.
            constraintSetForHeader.connect(
                    addCl.getId(), ConstraintSet.TOP,
                    cl.getId(), ConstraintSet.TOP, defaultPaddingBy2);

            // [ Add CL [Teachers] ] always start to start parent.
            constraintSetForHeader.connect(
                    addCl.getId(), ConstraintSet.START,
                    cl.getId(), ConstraintSet.START, defaultPaddingBy2);
        }

        //Current Person image TOP to BOTTOM of [ Add CL ] (always)
        constraintSetForHeader.connect(
                R.id.item_studentlist_student_simple_student_image,ConstraintSet.TOP,
                addCl.getId(), ConstraintSet.BOTTOM, defaultPaddingBy2);

        //Current Person title TOP to BOTTOM of [ Add CL ] (always)
        constraintSetForHeader.connect(
                R.id.item_studentlist_student_simple_student_title, ConstraintSet.TOP,
                addCl.getId(), ConstraintSet.BOTTOM, defaultPaddingBy2);


        constraintSetForHeader.applyTo(cl);

        //Update the top for the next
        currentTop = addCl.getId();

        if(role == ClazzMember.ROLE_TEACHER){
            teacherAdded = true;
        }

    }

}
