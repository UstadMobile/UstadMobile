package com.ustadmobile.port.android.view;

import android.annotation.SuppressLint;
import android.app.Activity;
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

import static com.ustadmobile.port.android.view.PersonEditActivity.ADD_PERSON_ICON;
import static com.ustadmobile.port.android.view.PersonEditActivity.DEFAULT_PADDING;

public class PersonWithEnrollmentRecyclerAdapter
        extends PagedListAdapter<PersonWithEnrollment,
        PersonWithEnrollmentRecyclerAdapter.ClazzLogDetailViewHolder> {

    private Context theContext;
    private Activity theActivity;
    private Fragment theFragment;
    private CommonHandlerPresenter mPresenter;
    private boolean showAttendance;
    private boolean showEnrollment;

    private int currentTop = -1;

    private int headingIdT;
    private int headingImageIdT;
    private int addClazzMemberIdT;
    private int hLineIdT;
    private int headingIdS;
    private int headingImageIdS;
    private int addClazzMemberIdS;
    private int hLineIdS;

    private int addClazzMemberCLId = View.generateViewId();
    ConstraintLayout addClazzMemberCL;

    PersonWithEnrollment previousPerson = null;

    @SuppressLint("UseSparseArrays")
    private HashMap<Long, Boolean> checkBoxHM = new HashMap<>();

    class ClazzLogDetailViewHolder extends RecyclerView.ViewHolder{
        ClazzLogDetailViewHolder(View itemView){
            super(itemView);
        }
    }

    @Override
    public int getItemCount() {

        if(super.getItemCount() == 0){
        }
        return super.getItemCount();
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

    public int getResourceId(String pVariableName, String pResourcename, String pPackageName)
    {
        try {
            if(theActivity != null){
                return theActivity.getResources().getIdentifier(pVariableName, pResourcename,
                        pPackageName);
            }else {
                return theFragment.getResources().getIdentifier(pVariableName, pResourcename,
                        pPackageName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public String getText(int id){
        if(theActivity != null){
            return theActivity.getText(id).toString();
        }else{
            return theFragment.getText(id).toString();
        }
    }

    public int getDp(int dp){
        if(theActivity != null){
            return Math.round(
                    dp * theActivity.getResources().getDisplayMetrics().density);

        }else{
            return Math.round(
                    dp * theFragment.getResources().getDisplayMetrics().density);
        }
    }

    public int getIconRes(String res){
        if(theActivity != null){
            return getResourceId(res,
                    "drawable", theActivity.getPackageName());
        }else{
            return getResourceId(res,
                    "drawable", theFragment.getActivity().getPackageName());
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

        //1. Check if empty of not.

        //2. Check if start of query

        //3. If start -> Create Teacher heading as well as add Teacher entry.

        //4. If not, check if personWithEnrollment is a student and previousPerson is teacher

        //5. If so, add Student and add Student entry.

        //6. Add the Student entry.

        //7. If  not, add teacher entry

        //1. Check if empty or not.

        PersonWithEnrollment personWithEnrollment = getItem(position);

        //if this is the first student... add header for students, add student button, etc

        assert personWithEnrollment != null;
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
        cl.setOnClickListener(v ->
                mPresenter.handleCommonPressed(personUid));

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

        }

        if(showEnrollment){

            //Get checkbox and set it to visible.
            CheckBox checkBox =
                    holder.itemView.findViewById(R.id.item_studentlist_student_simple_student_checkbox);
            checkBox.setVisibility(View.VISIBLE);

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

        }else{

            if (position == 0) {//First Entry. Add Teacher and Add Teacher item
                addHeadingAndNew(cl, ClazzMember.ROLE_TEACHER);

                if(personWithEnrollment.getClazzMemberRole() == ClazzMember.ROLE_STUDENT){
                    addHeadingAndNew(cl, ClazzMember.ROLE_STUDENT);
                }

            } else {
                previousPerson = getItem(position - 1);
                if (previousPerson.getClazzMemberRole() == ClazzMember.ROLE_TEACHER &&
                        personWithEnrollment.getClazzMemberRole() == ClazzMember.ROLE_STUDENT) {

                    //Add student
                    addHeadingAndNew(cl, ClazzMember.ROLE_STUDENT);
                }
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

        View headingView = holder.itemView.findViewById(headingIdT);
        View headingImageView = holder.itemView.findViewById(headingImageIdT);
        View addClazzMemberView = holder.itemView.findViewById(addClazzMemberIdT);
        View hLineView = holder.itemView.findViewById(hLineIdT);

        View headingViewS = holder.itemView.findViewById(headingIdS);
        View headingImageViewS = holder.itemView.findViewById(headingImageIdS);
        View addClazzMemberViewS = holder.itemView.findViewById(addClazzMemberIdS);
        View hLineViewS = holder.itemView.findViewById(hLineIdS);

        cl.removeView(headingView);
        cl.removeView(headingImageView);
        cl.removeView(addClazzMemberView);
        cl.removeView(hLineView);

        cl.removeView(headingViewS);
        cl.removeView(headingImageViewS);
        cl.removeView(addClazzMemberViewS);
        cl.removeView(hLineViewS);

        if(headingView != null){
            headingView.setVisibility(View.INVISIBLE);
        }
        if(headingImageView != null){
            headingImageView.setVisibility(View.INVISIBLE);
        }
        if(addClazzMemberView != null){
            addClazzMemberView.setVisibility(View.INVISIBLE);
        }
        if(hLineView != null){
            hLineView.setVisibility(View.INVISIBLE);
        }

        if(headingViewS != null){
            headingViewS.setVisibility(View.INVISIBLE);
        }
        if(headingImageViewS != null){
            headingImageViewS.setVisibility(View.INVISIBLE);
        }
        if(addClazzMemberViewS != null){
            addClazzMemberViewS.setVisibility(View.INVISIBLE);
        }
        if(hLineViewS != null){
            hLineViewS.setVisibility(View.INVISIBLE);
        }

    }

    /**
     * Adds a heading depending on role given
     * @param cl    The Constraint layout where the list will be in.
     * @param role  The role (Teacher or Student) as per ClazzMember.ROLE_*
     */
    public void addHeadingAndNew(ConstraintLayout cl, int role){

        TextView clazzMemberRoleHeadingTextView = new TextView(theContext);
        clazzMemberRoleHeadingTextView.setTextColor(Color.BLACK);
        clazzMemberRoleHeadingTextView.setTextSize(16);
        clazzMemberRoleHeadingTextView.setLeft(8);

        int addIconResId = getIconRes(ADD_PERSON_ICON);
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


        if (role == ClazzMember.ROLE_STUDENT) {
            addClazzMemberTextView.setText(getText(R.string.add_student));
            clazzMemberRoleHeadingTextView.setText(getText(R.string.students_literal));
            addClazzMemberTextView.setOnClickListener(v -> mPresenter.handleCommonPressed(-1L));

            //Storing in separate variables so we can remove them.
            headingIdS = headingId;
            headingImageIdS = headingImageId;
            addClazzMemberIdS = addClazzMemberId;
            hLineIdS = hLineId;

        } else {

            addClazzMemberTextView.setText(getText(R.string.add_teacher));
            clazzMemberRoleHeadingTextView.setText(getText(R.string.teachers_literal));
            addClazzMemberTextView.setOnClickListener(v -> mPresenter.handleSecondaryPressed(-1L));
            currentTop = cl.getId();

            //Storing in separate variables so we can remove them.
            headingIdT = headingId;
            headingImageIdT = headingImageId;
            addClazzMemberIdT = addClazzMemberId;
            hLineIdT = hLineId;
        }

        //Set ids for all components.
        clazzMemberRoleHeadingTextView.setId(headingId);
        addClazzMemberTextView.setId(addClazzMemberId);
        horizontalLine.setId(hLineId);
        addPersonImageView.setId(headingImageId);
        
        //Add view components to the CL
        cl.addView(clazzMemberRoleHeadingTextView);
        cl.addView(addPersonImageView);
        cl.addView(addClazzMemberTextView);
        cl.addView(horizontalLine);

        int defaultPadding = getDp(DEFAULT_PADDING);
        int defaultPaddingBy2 = getDp(DEFAULT_PADDING/2);

        ConstraintSet constraintSetForHeader = new ConstraintSet();
        constraintSetForHeader.clone(cl);

        //[Teachers / Students Heading] TOP to TOP of whichever the top
        //  (can be Parent or horizontal line of previous item)
        constraintSetForHeader.connect(
                headingId, ConstraintSet.TOP,
                currentTop, ConstraintSet.TOP,
                defaultPaddingBy2);
        //[Teachers / Students Heading] START to START of PARENT (always)
        constraintSetForHeader.connect(
                headingId, ConstraintSet.START,
                cl.getId(), ConstraintSet.START,
                defaultPaddingBy2);

        //[Add teacher/student Icon] START to START of Parent (always)
        constraintSetForHeader.connect(
                headingImageId, ConstraintSet.START,
                cl.getId(), ConstraintSet.START, defaultPadding);

        //[Add teacher/student Icon] TOP to BOTTOM of Heading
        constraintSetForHeader.connect(
                headingImageId, ConstraintSet.TOP,
                headingId, ConstraintSet.BOTTOM, defaultPaddingBy2);

        //[Add teacher/student Text]  START to Icon END (always)
        constraintSetForHeader.connect(
                addClazzMemberId, ConstraintSet.START,
                headingImageId, ConstraintSet.END, defaultPadding);

        //[Add teacher/student Text] TOP to [Teacher / Students Heading] Bottom (always)
        constraintSetForHeader.connect(
                addClazzMemberId, ConstraintSet.TOP,
                headingId, ConstraintSet.BOTTOM, defaultPaddingBy2);

        //[Add Teacher/Student HLine] TOP to [Teacher / Student Icon] BOTTON (always)
        constraintSetForHeader.connect(
                hLineId, ConstraintSet.TOP,
                headingImageId, ConstraintSet.BOTTOM, defaultPaddingBy2);

        //[Add Teacher/Student HLine] START to Parent (always)
        constraintSetForHeader.connect(hLineId, ConstraintSet.START,
                cl.getId(), ConstraintSet.START, 0);

        //Current Person image TOP to BOTTOM of horizontal line (always)
        constraintSetForHeader.connect(
                R.id.item_studentlist_student_simple_student_image,ConstraintSet.TOP,
                hLineId, ConstraintSet.BOTTOM, defaultPaddingBy2);

        //Current Person title TOP to BOTTOM of horizontal line (always)
        constraintSetForHeader.connect(
                R.id.item_studentlist_student_simple_student_title, ConstraintSet.TOP,
                hLineId, ConstraintSet.BOTTOM, defaultPaddingBy2);

        constraintSetForHeader.applyTo(cl);

        currentTop = hLineId;

    }
}