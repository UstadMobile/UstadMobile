package com.ustadmobile.port.android.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.paging.PagedList;
import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.CommonHandlerPresenter;
import com.ustadmobile.lib.db.entities.ClazzMember;
import com.ustadmobile.lib.db.entities.PersonWithEnrollment;

import java.util.HashMap;
import java.util.Objects;

import static com.ustadmobile.port.android.view.PersonEditActivity.DEFAULT_PADDING;

/**
 * Common recycler adapter for people with enrollment, attendance and title. Used to show
 * list of students, teachers, people, etc throughout the application. We can initialise this
 * adapter to show enrollment for a particular class, show attendance, or add "Adders" or not. The
 * adapter gets called either from an activity or fragment and the must also have a reference to
 * a CommonHandlerPresenter extended presenter for actions on every item.
 *
 */
public class PersonWithEnrollmentRecyclerAdapter
        extends PagedListAdapter<PersonWithEnrollment,
        PersonWithEnrollmentRecyclerAdapter.ClazzLogDetailViewHolder> {

    private Context theContext;
    private Activity theActivity;
    private Fragment theFragment;
    private CommonHandlerPresenter mPresenter;
    private boolean showAttendance;
    private boolean showEnrollment;
    private boolean isEmpty = false;
    private boolean addStudentLast = false;

    private int currentTop = -1;
    private boolean teacherAdded = false;

    private int addCMCLT, addCMCLS;

    @SuppressLint("UseSparseArrays")
    private HashMap<Long, Boolean> checkBoxHM = new HashMap<>();

    void submitListCustom(PagedList<PersonWithEnrollment> personWithEnrollments) {
        super.submitList(personWithEnrollments);

        if (personWithEnrollments.size() == 0 ){
            isEmpty = true;
        }
    }

    class ClazzLogDetailViewHolder extends RecyclerView.ViewHolder{
        ClazzLogDetailViewHolder(View itemView){
            super(itemView);
        }
    }

    PersonWithEnrollmentRecyclerAdapter(
            @NonNull DiffUtil.ItemCallback<PersonWithEnrollment> diffCallback, Context context,
            Activity activity, CommonHandlerPresenter presenter, boolean attendance,
            boolean enrollment){
        super(diffCallback);
        theContext = context;
        theActivity = activity;
        mPresenter = presenter;
        showAttendance = attendance;
        showEnrollment = enrollment;
    }

    PersonWithEnrollmentRecyclerAdapter(
            @NonNull DiffUtil.ItemCallback<PersonWithEnrollment> diffCallback, Context context,
            Fragment fragment, CommonHandlerPresenter presenter, boolean attendance,
            boolean enrollment){
        super(diffCallback);
        theContext = context;
        theFragment = fragment;
        mPresenter = presenter;
        showAttendance = attendance;
        showEnrollment = enrollment;
    }

    @NonNull
    @Override
    public PersonWithEnrollmentRecyclerAdapter.ClazzLogDetailViewHolder
            onCreateViewHolder(@NonNull ViewGroup parent, int viewType){

        View clazzLogDetailListItem =
                LayoutInflater.from(theContext).inflate(
                        R.layout.item_studentlistenroll_student, parent, false);
        return new PersonWithEnrollmentRecyclerAdapter.ClazzLogDetailViewHolder(
                clazzLogDetailListItem);
    }

    /**
     * Get resource id of the drawable id
     *
     * @param pPackageName  The package calling it
     * @return  The resource id
     */
    private int getAddPersonIconResourceId(String pPackageName)
    {
        try {
            if(theActivity != null){
                return theActivity.getResources().getIdentifier(PersonEditActivity.ADD_PERSON_ICON,
                        "drawable", pPackageName);
            }else {
                return theFragment.getResources().getIdentifier(PersonEditActivity.ADD_PERSON_ICON,
                        "drawable", pPackageName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Get text weather activity or a fragment calls this adapter
     *
     * @param id    The text id
     * @return  The string
     */
    public String getText(int id){
        if(theActivity != null){
            return theActivity.getText(id).toString();
        }else{
            return theFragment.getText(id).toString();
        }
    }

    /**
     * Get dp from pixel
     *
     * @param dp the pixels
     * @return  The dp
     */
    private int getDp(int dp){
        if(theActivity != null){
            return Math.round(
                    dp * theActivity.getResources().getDisplayMetrics().density);

        }else{
            return Math.round(
                    dp * theFragment.getResources().getDisplayMetrics().density);
        }
    }

    /**
     * Get ADD_PERSON_ICON as resource
     *
     * @return The resource
     */
    private int getAddPersonIconRes(){
        if(theActivity != null){
            return getAddPersonIconResourceId(
                    theActivity.getPackageName());
        }else{
            return getAddPersonIconResourceId(
                    Objects.requireNonNull(theFragment.getActivity()).getPackageName());
        }
    }

    /**
     * This method sets the elements after it has been obtained for that item'th position.
     *  Every item in the recycler view will have set its colors if no attendance status is set.
     *  every attendance button will have it-self mapped to tints on activation.
     *
     * @param holder Holder that has the view
     * @param position  The position in the recycler view.
     */
    @Override
    public void onBindViewHolder(
            @NonNull PersonWithEnrollmentRecyclerAdapter.ClazzLogDetailViewHolder holder,
            int position){


        PersonWithEnrollment personWithEnrollment = getItem(position);

        assert personWithEnrollment != null;
        if(personWithEnrollment == null){
            return;
        }

        addStudentLast = false;

        String studentName = personWithEnrollment.getFirstNames() + " " +
                personWithEnrollment.getLastName();
        Long personUid = personWithEnrollment.getPersonUid();

        TextView studentNameTextView =
                holder.itemView.findViewById(R.id.item_studentlist_student_simple_student_title);
        studentNameTextView.setText(studentName);

        ImageView trafficLight = holder.itemView
                .findViewById(R.id.item_studentlist_student_simple_attendance_trafficlight);
        TextView attendanceTextView =
                holder.itemView.findViewById(R.id.item_studentlist_student_simple_attendance_percentage);

        ConstraintLayout cl = holder.itemView.findViewById(R.id.item_studentlist_student_cl);
        //If you want the whole CL to be clickable
        //cl.setOnClickListener(v ->
        //        mPresenter.handleCommonPressed(personUid));

        studentNameTextView.setOnClickListener(v -> mPresenter.handleCommonPressed(personUid));

        //Remove previous add clazz member views
        removeAllAddClazzMemberView(cl, holder);

        if(showAttendance){
            long attendancePercentage =
                    (long) (personWithEnrollment.getAttendancePercentage() * 100);

            String attendanceStringLiteral;
            if(theActivity != null){
                attendanceStringLiteral = theActivity.getText(R.string.attendance).toString();
            }else{
                attendanceStringLiteral = theFragment.getText(R.string.attendance).toString();
            }

            String studentAttendancePercentage = attendancePercentage +
                    "% " + attendanceStringLiteral;

            trafficLight.setVisibility(View.VISIBLE);
            if(attendancePercentage > 75L){
                trafficLight.setColorFilter(ContextCompat.getColor(theContext,
                        R.color.traffic_green));
            }else if(attendancePercentage > 50L){
                trafficLight.setColorFilter(ContextCompat.getColor(theContext,
                        R.color.traffic_orange));
            }else{
                trafficLight.setColorFilter(ContextCompat.getColor(theContext,
                        R.color.traffic_red));
            }

            attendanceTextView.setVisibility(View.VISIBLE);
            attendanceTextView.setText(studentAttendancePercentage);
        }else{

            //Change the constraint layout so that the hidden bits are not empty spaces.

            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(cl);

            //connect divider's top to image's bottom
            constraintSet.connect(R.id.item_studentlist_student_simple_horizontal_divider,
                    ConstraintSet.TOP, R.id.item_studentlist_student_simple_student_image,
                    ConstraintSet.BOTTOM, 16);
            constraintSet.connect(R.id.item_studentlist_student_simple_attendance_percentage,
                    ConstraintSet.TOP, R.id.item_studentlist_student_simple_student_title,
                    ConstraintSet.BOTTOM, 0);
            constraintSet.connect(R.id.item_studentlist_student_simple_attendance_trafficlight,
                    ConstraintSet.TOP, R.id.item_studentlist_student_simple_student_title,
                    ConstraintSet.BOTTOM, 0);
            constraintSet.applyTo(cl);

            //or just leave the spaces in hopes of better performance ?
            //Update it doesnt really make it quicker

        }

        if(showEnrollment){

            //Get checkbox and set it to visible.
            CheckBox checkBox =
                    holder.itemView.findViewById(R.id.item_studentlist_student_simple_student_checkbox);
            checkBox.setVisibility(View.VISIBLE);
            checkBox.setTextColor(Color.BLACK);
            checkBox.setSystemUiVisibility(View.VISIBLE);
            checkBox.setCursorVisible(true);


            //Get current person's enrollment w.r.t. this class. (Its either set or null (not enrolled)
            boolean personWithEnrollmentBoolean = false;
            if (personWithEnrollment.getEnrolled() != null){
                personWithEnrollmentBoolean = personWithEnrollment.getEnrolled();
            }

            //To preserve checkboxes, add this enrollment to the Map.
            checkBoxHM.put(personWithEnrollment.getPersonUid(), personWithEnrollmentBoolean);
            //set the value of the check according to the value..
            checkBox.setChecked(checkBoxHM.get(personWithEnrollment.getPersonUid()));

            //Add a change listener to the checkbox
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                    checkBox.setChecked(isChecked));

            checkBox.setOnClickListener(v -> {
                final boolean isChecked = checkBox.isChecked();
                HashMap<PersonWithEnrollment, Boolean> arguments = new HashMap<>();
                arguments.put(personWithEnrollment, isChecked);
                mPresenter.handleSecondaryPressed(arguments.entrySet().iterator().next());
            });

        }

        if(!showEnrollment && showAttendance){

            if (position == 0) {//First Entry. Add Teacher and Add Teacher item
                addHeadingAndNew(cl, ClazzMember.ROLE_TEACHER);

                if(personWithEnrollment.getClazzMemberRole() == ClazzMember.ROLE_STUDENT){

                    addHeadingAndNew(cl, ClazzMember.ROLE_STUDENT);
                }

                int nextPos = position + 1;
                if(personWithEnrollment.getClazzMemberRole() == ClazzMember.ROLE_TEACHER &&
                        getItemCount() == nextPos){
                    addStudentLast = true;
                }

            } else {
                PersonWithEnrollment previousPerson = getItem(position - 1);
                assert previousPerson != null;
                if (previousPerson.getClazzMemberRole() == ClazzMember.ROLE_TEACHER &&
                        personWithEnrollment.getClazzMemberRole() == ClazzMember.ROLE_STUDENT) {


                    //Add student
                    addHeadingAndNew(cl, ClazzMember.ROLE_STUDENT);
                }

            }
        }

        if(personWithEnrollment.getClazzMemberRole() == ClazzMember.ROLE_TEACHER){

            trafficLight.setVisibility(View.INVISIBLE);
            attendanceTextView.setVisibility(View.INVISIBLE);

            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(cl);

            //connect divider's top to image's bottom
            constraintSet.connect(R.id.item_studentlist_student_simple_horizontal_divider,
                    ConstraintSet.TOP, R.id.item_studentlist_student_simple_student_image,
                    ConstraintSet.BOTTOM, 16);
            constraintSet.connect(R.id.item_studentlist_student_simple_attendance_percentage,
                    ConstraintSet.TOP, R.id.item_studentlist_student_simple_student_title,
                    ConstraintSet.BOTTOM, 0);
            constraintSet.connect(R.id.item_studentlist_student_simple_attendance_trafficlight,
                    ConstraintSet.TOP, R.id.item_studentlist_student_simple_student_title,
                    ConstraintSet.BOTTOM, 0);
            constraintSet.applyTo(cl);

        }

        if(getItemCount() == position+1) {
            if (addStudentLast) {
                addHeadingAndNew(cl, ClazzMember.ROLE_STUDENT);
            }
        }
    }


    /**
     * Removes old Add ClazzMember views
     *
     * @param cl    The constraint layout to search in
     * @param holder    The holder that has the itemView
     */
    private void removeAllAddClazzMemberView(ConstraintLayout cl,
                     @NonNull PersonWithEnrollmentRecyclerAdapter.ClazzLogDetailViewHolder holder){

        //Get Clazz Member layout for student and teacher
        View addCMCLViewS = holder.itemView.findViewById(addCMCLS);
        View addCMCLViewT = holder.itemView.findViewById(addCMCLT);

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
     * Adds a heading depending on role given
     * @param cl    The Constraint layout where the list will be in.
     * @param role  The role (Teacher or Student) as per ClazzMember.ROLE_*
     */
    private void addHeadingAndNew(ConstraintLayout cl, int role){

        ConstraintLayout addCl = new ConstraintLayout(theContext);
        int defaultPadding = getDp(DEFAULT_PADDING);
        int defaultPaddingBy2 = getDp(DEFAULT_PADDING/2);

        TextView clazzMemberRoleHeadingTextView = new TextView(theContext);
        clazzMemberRoleHeadingTextView.setTextColor(Color.BLACK);
        clazzMemberRoleHeadingTextView.setTextSize(16);
        clazzMemberRoleHeadingTextView.setLeft(8);

        int addIconResId = getAddPersonIconRes();
        ImageView addPersonImageView = new ImageView(theContext);
        addPersonImageView.setImageResource(addIconResId);

        TextView addClazzMemberTextView = new TextView(theContext);
        addClazzMemberTextView.setTextColor(Color.BLACK);
        addClazzMemberTextView.setTextSize(16);
        addClazzMemberTextView.setLeft(8);

        //Horizontal line
        View horizontalLine = new View(theContext);
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