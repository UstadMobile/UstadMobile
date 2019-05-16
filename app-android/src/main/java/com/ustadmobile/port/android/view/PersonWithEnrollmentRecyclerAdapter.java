package com.ustadmobile.port.android.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.CommonHandlerPresenter;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.lib.db.entities.ClazzMember;
import com.ustadmobile.lib.db.entities.PersonWithEnrollment;

import java.io.File;
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
    private boolean groupEnrollment = false;

    private boolean showAddStudent = false;
    private boolean showAddTeacher = false;

    private int currentTop = -1;
    private boolean teacherAdded = false;

    private boolean reportMode = false;

    private boolean groupByClass = false;

    private boolean hideHeading = false;

    private int headingCLId;

    private int addCMCLT, addCMCLS;

    private static final int IMAGE_PERSON_THUMBNAIL_WIDTH = 26;

    @SuppressLint("UseSparseArrays")
    private HashMap<Long, Boolean> checkBoxHM = new HashMap<>();

    class ClazzLogDetailViewHolder extends RecyclerView.ViewHolder{
        ClazzLogDetailViewHolder(View itemView){
            super(itemView);
        }
    }

    PersonWithEnrollmentRecyclerAdapter(
            @NonNull DiffUtil.ItemCallback<PersonWithEnrollment> diffCallback, Context context,
            Activity activity, CommonHandlerPresenter presenter, boolean attendance,
            boolean enrollment, boolean enrollToGroup){
        super(diffCallback);
        theContext = context;
        theActivity = activity;
        mPresenter = presenter;
        showAttendance = attendance;
        showEnrollment = enrollment;
        groupEnrollment = enrollToGroup;
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

    PersonWithEnrollmentRecyclerAdapter(
            @NonNull DiffUtil.ItemCallback<PersonWithEnrollment> diffCallback, Context context,
            Activity activity, CommonHandlerPresenter presenter, boolean attendance,
            boolean enrollment, boolean rmode, boolean classGrouped){
        super(diffCallback);
        theContext = context;
        theActivity = activity;
        mPresenter = presenter;
        showAttendance = attendance;
        showEnrollment = enrollment;
        reportMode = rmode;
        groupByClass = classGrouped;
    }

    PersonWithEnrollmentRecyclerAdapter(
            @NonNull DiffUtil.ItemCallback<PersonWithEnrollment> diffCallback, Context context,
            Activity activity, CommonHandlerPresenter presenter, boolean attendance,
            boolean enrollment, boolean rmode, boolean classGrouped, boolean hideHeading){
        super(diffCallback);
        theContext = context;
        theActivity = activity;
        mPresenter = presenter;
        showAttendance = attendance;
        showEnrollment = enrollment;
        reportMode = rmode;
        groupByClass = classGrouped;
        this.hideHeading = hideHeading;
    }


    void setShowAddStudent(boolean showAddStudent) {
        this.showAddStudent = showAddStudent;
    }

    void setShowAddTeacher(boolean showAddTeacher) {
        this.showAddTeacher = showAddTeacher;
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

        //Get person with enrollment and other info
        PersonWithEnrollment personWithEnrollment = getItem(position);
        assert personWithEnrollment != null;

        //Flag that says that complete recyclerview has only 1 item and its a teacher. Used to add
        // "Add Students" as well before we finish onBindView for the teacher.
        boolean onlyTeacherExists = false;

        TextView studentNameTextView =
                holder.itemView.findViewById(R.id.item_studentlist_student_simple_student_title);
        ImageView personPicture =
                holder.itemView.findViewById(R.id.item_studentlist_student_simple_student_image);
        ImageView trafficLight = holder.itemView
                .findViewById(R.id.item_studentlist_student_simple_attendance_trafficlight);
        TextView attendanceTextView =
                holder.itemView.findViewById(R.id.item_studentlist_student_simple_attendance_percentage);
        ConstraintLayout cl = holder.itemView.findViewById(R.id.item_studentlist_student_cl);

        CheckBox checkBox =
                holder.itemView.findViewById(R.id.item_studentlist_student_simple_student_checkbox);
        if(groupEnrollment){
            checkBox.setText(R.string.enroll_group_member);
        }else{
            checkBox.setText(R.string.enroll_in_class);
        }
        ImageView callImageView =
                holder.itemView.findViewById(R.id.item_studentlist_student_simple_call_iv);

        //Update person name :
        String firstName = "";
        String lastName = "";
        if(personWithEnrollment == null){
            return;
        }
        if(personWithEnrollment.getFirstNames() != null){
            firstName = personWithEnrollment.getFirstNames();
        }
        if(personWithEnrollment.getLastName() != null){
            lastName = personWithEnrollment.getLastName();
        }
        String studentName = firstName + " " + lastName;
        studentNameTextView.setText(studentName);

        //Name click listener:
        Long personUid = personWithEnrollment.getPersonUid();
        studentNameTextView.setOnClickListener(v -> mPresenter.handleCommonPressed(personUid));

        //HEADING:
        //Remove previous add clazz member views
        if(addCMCLS != 0 || addCMCLT != 0) {
            removeAllAddClazzMemberView(cl, holder);
        }

        //PICTURE : Add picture to person
        String imagePath = "";
        long personPictureUid = personWithEnrollment.getPersonPictureUid();
        if (personPictureUid != 0) {
            imagePath = UmAppDatabase.getInstance(theContext).getPersonPictureDao()
                    .getAttachmentPath(personPictureUid);
        }

        if(imagePath != null && !imagePath.isEmpty())
            setPictureOnView(imagePath, personPicture);
        else
            personPicture.setImageResource(R.drawable.ic_person_black_new_24dp);

        //ENROLLMENT
        if(showEnrollment){
            checkBox.setVisibility(View.VISIBLE);
            checkBox.setTextColor(Color.BLACK);
            checkBox.setSystemUiVisibility(View.VISIBLE);
            checkBox.setCursorVisible(true);

            //Get current person's enrollment w.r.t. this class.
            // (Its either set or null (not enrolled))
            boolean personWithEnrollmentBoolean;
            if (personWithEnrollment.getEnrolled() != null){
                personWithEnrollmentBoolean = personWithEnrollment.getEnrolled();
            }else{
                personWithEnrollmentBoolean = false;
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
            //If you want the whole CL to be clickable
            cl.setOnClickListener(v ->
                    mPresenter.handleCommonPressed(personUid));
        }

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
                    ConstraintSet.TOP, R.id.item_studentlist_student_simple_student_title,
                    ConstraintSet.BOTTOM, 16);
            constraintSet.connect(R.id.item_studentlist_student_simple_attendance_percentage,
                    ConstraintSet.TOP, R.id.item_studentlist_student_simple_student_title,
                    ConstraintSet.BOTTOM, 0);
            constraintSet.connect(R.id.item_studentlist_student_simple_attendance_trafficlight,
                    ConstraintSet.TOP, R.id.item_studentlist_student_simple_student_title,
                    ConstraintSet.BOTTOM, 0);
            constraintSet.applyTo(cl);

            //or just leave the spaces in hopes of better performance ?
            //Update it doesn't really make it quicker

        }
        if(reportMode){
            checkBox.setVisibility(View.GONE);

            callImageView.setVisibility(View.VISIBLE);
            callImageView.setOnClickListener(v ->
                    mPresenter.handleSecondaryPressed(personWithEnrollment));


        }else{
            callImageView.setVisibility(View.GONE);
        }

        if(!showEnrollment && showAttendance){

            if (position == 0) {//First Entry. Add Teacher and Add Teacher item
                if(!reportMode && !hideHeading) {
                    addHeadingAndNew(cl, ClazzMember.ROLE_TEACHER, showAddTeacher, holder);
                }else{
                    int x;
                }

                if (personWithEnrollment.getClazzMemberRole() == ClazzMember.ROLE_STUDENT) {

                    if(!reportMode && !hideHeading) {
                        addHeadingAndNew(cl, ClazzMember.ROLE_STUDENT, showAddStudent, holder);
                    }
                    else {
                        int x;
                    }

                }else{
                    int x;
                }

                //If first item is a teacher and there are no more items:
                if (personWithEnrollment.getClazzMemberRole() == ClazzMember.ROLE_TEACHER &&
                        getItemCount() == 1) {
                    onlyTeacherExists = true;
                }else{
                    int x;
                }

            } else {
                PersonWithEnrollment previousPerson = getItem(position - 1);
                assert previousPerson != null;

                if (previousPerson.getClazzMemberRole() == ClazzMember.ROLE_TEACHER &&
                        personWithEnrollment.getClazzMemberRole() == ClazzMember.ROLE_STUDENT) {

                    //Add student
                    if(!reportMode && !hideHeading) {
                        addHeadingAndNew(cl, ClazzMember.ROLE_STUDENT, showAddStudent, holder);
                    }else{
                        int x;
                    }
                }else{
                    int x;
                }

            }

        }

        if(groupByClass){

            long thisClazzUid = personWithEnrollment.getClazzUid();
            removeHeading(cl, headingCLId, holder);

            if(position == 0){
                addHeading(cl, personWithEnrollment.getClazzName(), holder);
            }else{
                PersonWithEnrollment previousPersonWithEnrollment = getItem(position - 1);
                long previousClazzUid = previousPersonWithEnrollment.getClazzUid();

                if(thisClazzUid != previousClazzUid){
                    addHeading(cl, personWithEnrollment.getClazzName(), holder);
                }else{
                    addHeading(cl, "", holder);
                }

            }
        }else{
            removeHeading(cl, headingCLId, holder);
        }

        if(personWithEnrollment.getClazzMemberRole() == ClazzMember.ROLE_TEACHER){

            //Disable attendance for Teachers
            trafficLight.setVisibility(View.GONE);
            attendanceTextView.setVisibility(View.GONE);
        }else{
            //Do nothing;
            int x;
        }

        //If we reached the end of the rv and there is only one teacher in it,
        // add the "show Student" as well.
        if (onlyTeacherExists) {
            addHeadingAndNew(cl, ClazzMember.ROLE_STUDENT, showAddStudent, holder);
        }else{
            //Don't add anything.
            int x;

        }

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private void setPictureOnView(String imagePath, ImageView theImage) {

        Uri imageUri = Uri.fromFile(new File(imagePath));

        Picasso
                .get()
                .load(imageUri)
                .resize(dpToPxImagePerson(), dpToPxImagePerson())
                .noFade()
                .into(theImage);
    }


    private static int dpToPxImagePerson() {
        return (int) (PersonWithEnrollmentRecyclerAdapter.IMAGE_PERSON_THUMBNAIL_WIDTH
                * Resources.getSystem().getDisplayMetrics().density);
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


        //If view exists, set it to invisible/gone
        if(addCMCLViewS != null){
            addCMCLViewS.setVisibility(View.GONE);
        }
        if(addCMCLViewT != null){
            addCMCLViewT.setVisibility(View.GONE);
        }
    }

    /**
     * Removes old Add ClazzMember views
     *
     * @param cl    The constraint layout to search in
     * @param holder    The holder that has the itemView
     */
    private void removeAddTeacherAddView(ConstraintLayout cl,
                                             @NonNull PersonWithEnrollmentRecyclerAdapter.ClazzLogDetailViewHolder holder){

        //Get Clazz Member layout for student and teacher
        View addCMCLViewT = holder.itemView.findViewById(addCMCLT);

        //Remove the views
        cl.removeView(addCMCLViewT);

        if(addCMCLViewT != null){
            addCMCLViewT.setVisibility(View.GONE);
        }
    }

    /**
     * Removes old Add ClazzMember views
     *
     * @param cl    The constraint layout to search in
     * @param holder    The holder that has the itemView
     */
    private void removeAddStudentView(ConstraintLayout cl,
                                             @NonNull PersonWithEnrollmentRecyclerAdapter.ClazzLogDetailViewHolder holder){

        //Get Clazz Member layout for student and teacher
        View addCMCLViewS = holder.itemView.findViewById(addCMCLS);

        //Remove the views
        cl.removeView(addCMCLViewS);


        //If view exists, set it to invisible/gone
        if(addCMCLViewS != null){
            addCMCLViewS.setVisibility(View.GONE);
        }
    }

    private void removeHeading(ConstraintLayout cl, int headingId,
                   @NonNull PersonWithEnrollmentRecyclerAdapter.ClazzLogDetailViewHolder holder){
        View removeMe = holder.itemView.findViewById(headingId);

        if(removeMe!=null)
            removeMe.setVisibility(View.GONE);

        cl.removeView(removeMe);

    }

    /**
     * Adds a heading
     * @param mainCL    The Constraint layout where the list will be in.
     * @param heading  The heading
     */
    private void addHeading(ConstraintLayout mainCL, String heading,
                            @NonNull PersonWithEnrollmentRecyclerAdapter.ClazzLogDetailViewHolder holder){


        removeHeading(mainCL, headingCLId, holder);


        if(heading.isEmpty()){
            return;
        }

        ConstraintLayout headingCL = new ConstraintLayout(theContext);
        int defaultPaddingBy2 = getDp(DEFAULT_PADDING/2);

        //The Heading TextView
        TextView headingTV = new TextView(theContext);
        headingTV.setTextColor(Color.BLACK);
        headingTV.setTextSize(16);
        headingTV.setLeft(8);

        //Set ids for all components.
        headingCLId = View.generateViewId();
        headingTV.setId(View.generateViewId());
        headingCL.setId(headingCLId);

        //Set heading text
        headingTV.setText(heading);

        //Add these components to the new "add" Constraint Layout
        headingCL.addView(headingTV);

        ConstraintSet constraintSetForHeader2 = new ConstraintSet();
        constraintSetForHeader2.clone(headingCL);

        //Heading constraint to parent of the headingCL mainCL it is in.
        constraintSetForHeader2.connect(
                headingTV.getId(), ConstraintSet.TOP,
                headingCL.getId(), ConstraintSet.TOP,
                defaultPaddingBy2);

        constraintSetForHeader2.connect(
                headingTV.getId(), ConstraintSet.START,
                headingCL.getId(), ConstraintSet.START,
                defaultPaddingBy2);

        constraintSetForHeader2.applyTo(headingCL);

        //Add the heading CL to the main CL
        mainCL.addView(headingCL);

        ConstraintSet constraintSetForHeader = new ConstraintSet();
        constraintSetForHeader.clone(mainCL);

        //Current Person image TOP to BOTTOM of [ Heading CL ] (always)
        constraintSetForHeader.connect(
                R.id.item_studentlist_student_simple_student_image,ConstraintSet.TOP,
                headingCL.getId(), ConstraintSet.BOTTOM, defaultPaddingBy2);

        //Current Person title TOP to BOTTOM of [ Add CL ] (always)
        constraintSetForHeader.connect(
                R.id.item_studentlist_student_simple_student_title, ConstraintSet.TOP,
                headingCL.getId(), ConstraintSet.BOTTOM, defaultPaddingBy2);

        constraintSetForHeader.applyTo(mainCL);



    }

    /**
     * Adds a heading depending on role given
     * @param cl    The Constraint layout where the list will be in.
     * @param role  The role (Teacher or Student) as per ClazzMember.ROLE_*
     */
    private void addHeadingAndNew(ConstraintLayout cl, int role, boolean showAdd,
                  @NonNull PersonWithEnrollmentRecyclerAdapter.ClazzLogDetailViewHolder holder){

        //Testing if improves:
        if(role == ClazzMember.ROLE_TEACHER){
            removeAddStudentView(cl, holder);
        }else{
            removeAddTeacherAddView(cl, holder);
        }

        ConstraintLayout addCl = new ConstraintLayout(theContext);
        int defaultPadding = getDp(DEFAULT_PADDING);
        int defaultPaddingBy2 = getDp(DEFAULT_PADDING/2);

        TextView clazzMemberRoleHeadingTextView = new TextView(theContext);
        clazzMemberRoleHeadingTextView.setTextColor(Color.BLACK);
        clazzMemberRoleHeadingTextView.setTextSize(16);
        clazzMemberRoleHeadingTextView.setLeft(8);

        int addIconResId = getAddPersonIconRes();
        AppCompatImageView addPersonImageView = new AppCompatImageView(theContext);
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
        if(role == ClazzMember.ROLE_STUDENT && showAddStudent) {
            addCl.addView(addPersonImageView);
            if(showAdd)
                addCl.addView(addClazzMemberTextView);
            else{
                View blankView = new View(theContext);
                blankView.setVisibility(View.GONE);
                addCl.addView(blankView);
            }
            addCl.addView(horizontalLine);
        }
        if(role == ClazzMember.ROLE_TEACHER && showAddTeacher) {
            addCl.addView(addPersonImageView);
            if(showAdd)
                addCl.addView(addClazzMemberTextView);
            else{
                View blankView = new View(theContext);
                blankView.setVisibility(View.GONE);
                addCl.addView(blankView);

            }

            addCl.addView(horizontalLine);
        }


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
        }else{
            teacherAdded = false;
        }

    }

}