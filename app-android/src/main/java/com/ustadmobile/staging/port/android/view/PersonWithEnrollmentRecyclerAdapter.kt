package com.ustadmobile.staging.port.android.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.CommonHandlerPresenter
import com.ustadmobile.core.db.dao.PersonPictureDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.lib.db.entities.ClazzMember
import com.ustadmobile.lib.db.entities.PersonWithEnrollment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import java.io.File
import java.util.*

/**
 * Common recycler adapter for people with enrollment, attendance and title. Used to show
 * list of students, teachers, people, etc throughout the application. We can initialise this
 * adapter to show enrollment for a particular class, show attendance, or add "Adders" or not. The
 * adapter gets called either from an activity or fragment and the must also have a reference to
 * a CommonHandlerPresenter extended presenter for actions on every item.
 *
 */
class PersonWithEnrollmentRecyclerAdapter : PagedListAdapter<PersonWithEnrollment,
        PersonWithEnrollmentRecyclerAdapter.ClazzLogDetailViewHolder> {

    private var theActivity: Activity? = null
    private var theFragment: Fragment? = null
    private var mPresenter: CommonHandlerPresenter<*>? = null
    private var showAttendance: Boolean = false
    private var showEnrollment: Boolean = false
    private var groupEnrollment = false

    private var showAddStudent = false
    private var showAddTeacher = false

    private var currentTop = -1
    private var currentDown = -1
    private var addTeacherAdded = false

    private var reportMode = false

    private var groupByClass = false

    private var hideHeading = false

    private var headingCLId: Int = 0

    private var addTeachersId: Int = 0
    private var addStudentsId: Int = 0

    private var personPictureDaoRepo: PersonPictureDao?=null

    var emptyAddStudents = false

    var addStudentAdded = false

    @SuppressLint("UseSparseArrays")
    private val checkBoxHM = HashMap<Long, Boolean>()

    /**
     * Get ADD_PERSON_ICON as resource
     *
     * @return The resource
     */
    private val addPersonIconRes: Int
        get() = if (theActivity != null) {
            getAddPersonIconResourceId(
                    theActivity!!.packageName)
        } else {
            getAddPersonIconResourceId(
                    theFragment!!.activity!!.packageName)
        }

    class ClazzLogDetailViewHolder(itemView: View, var imageLoadJob: Job? = null)
        : RecyclerView.ViewHolder(itemView)

    internal constructor(
            diffCallback: DiffUtil.ItemCallback<PersonWithEnrollment>, context: Context,
            activity: Activity, presenter: CommonHandlerPresenter<*>, attendance: Boolean,
            enrollment: Boolean, enrollToGroup: Boolean) : super(diffCallback) {
        theActivity = activity
        mPresenter = presenter
        showAttendance = attendance
        showEnrollment = enrollment
        groupEnrollment = enrollToGroup
    }

    internal constructor(
            diffCallback: DiffUtil.ItemCallback<PersonWithEnrollment>, context: Context,
            fragment: Fragment, presenter: CommonHandlerPresenter<*>, attendance: Boolean,
            enrollment: Boolean) : super(diffCallback) {
        theFragment = fragment
        mPresenter = presenter
        showAttendance = attendance
        showEnrollment = enrollment
    }

    internal constructor(
            diffCallback: DiffUtil.ItemCallback<PersonWithEnrollment>, context: Context,
            fragment: Fragment, presenter: CommonHandlerPresenter<*>, attendance: Boolean,
            enrollment: Boolean, addTeacherButton:Boolean, addStudentButton: Boolean ) : super(diffCallback) {
        theFragment = fragment
        mPresenter = presenter
        showAttendance = attendance
        showEnrollment = enrollment
        showAddTeacher = addTeacherButton
        showAddStudent = addStudentButton
    }

    internal constructor(context: Context,
            diffCallback: DiffUtil.ItemCallback<PersonWithEnrollment>,
            fragment: Fragment, presenter: CommonHandlerPresenter<*>, attendance: Boolean,
            enrollment: Boolean)
            : super(diffCallback as DiffUtil.ItemCallback<PersonWithEnrollment>) {
        theFragment = fragment
        mPresenter = presenter
        showAttendance = attendance
        showEnrollment = enrollment
    }

    internal constructor(
            diffCallback: DiffUtil.ItemCallback<PersonWithEnrollment>, context: Context,
            activity: Activity, presenter: CommonHandlerPresenter<*>, attendance: Boolean,
            enrollment: Boolean, rmode: Boolean, classGrouped: Boolean) : super(diffCallback) {
        theActivity = activity
        mPresenter = presenter
        showAttendance = attendance
        showEnrollment = enrollment
        reportMode = rmode
        groupByClass = classGrouped
    }

    internal constructor(
            diffCallback: DiffUtil.ItemCallback<PersonWithEnrollment>, context: Context,
            activity: Activity, presenter: CommonHandlerPresenter<*>, attendance: Boolean,
            enrollment: Boolean, rmode: Boolean, classGrouped: Boolean, hideHeading: Boolean)
            : super(diffCallback) {
        theActivity = activity
        mPresenter = presenter
        showAttendance = attendance
        showEnrollment = enrollment
        reportMode = rmode
        groupByClass = classGrouped
        this.hideHeading = hideHeading
    }


    internal fun setShowAddStudent(showAddStudent: Boolean) {
        this.showAddStudent = showAddStudent
    }

    internal fun setShowAddTeacher(showAddTeacher: Boolean) {
        this.showAddTeacher = showAddTeacher
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzLogDetailViewHolder {

        val clazzLogDetailListItem = LayoutInflater.from(parent.context).inflate(
                R.layout.item_studentlistenroll_student, parent, false)
        return ClazzLogDetailViewHolder(
                clazzLogDetailListItem)
    }

    /**
     * Get resource id of the drawable id
     *
     * @param pPackageName  The package calling it
     * @return  The resource id
     */
    private fun getAddPersonIconResourceId(pPackageName: String): Int {
//        try {
//            return if (theActivity != null) {
//                theActivity!!.resources.getIdentifier(PersonEditActivity.ADD_PERSON_ICON,
//                        "drawable", pPackageName)
//            } else {
//                theFragment!!.resources.getIdentifier(PersonEditActivity.ADD_PERSON_ICON,
//                        "drawable", pPackageName)
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            return -1
//        }

        return 0
    }

    /**
     * Get text weather activity or a fragment calls this adapter
     *
     * @param id    The text id
     * @return  The string
     */
    fun getText(id: Int): String {
        return if (theActivity != null) {
            theActivity!!.getText(id).toString()
        } else {
            theFragment!!.getText(id).toString()
        }
    }

    /**
     * Get dp from pixel
     *
     * @param dp the pixels
     * @return  The dp
     */
    private fun getDp(context: Context, dp: Int): Int {
        return if (theActivity != null) {
            Math.round(
                    dp * theActivity!!.resources.displayMetrics.density)

        } else {
            Math.round(
                    dp * theFragment!!.resources.displayMetrics.density)
        }
    }

    /**
     * This method sets the elements after it has been obtained for that item'th position.
     * Every item in the recycler view will have set its colors if no attendance status is set.
     * every attendance button will have it-self mapped to tints on activation.
     *
     * @param holder Holder that has the view
     * @param position  The position in the recycler view.
     */
    override fun onBindViewHolder(
            holder: ClazzLogDetailViewHolder,
            position: Int) {

        //Get person with enrollment and other info
        val personWithEnrollment = getItem(position)!!

        val studentNameTextView =
                holder.itemView.findViewById<TextView>(
                        R.id.item_studentlist_student_simple_student_title)
        val personPicture =
                holder.itemView.findViewById<ImageView>(
                        R.id.item_studentlist_student_simple_student_image)
        val trafficLight =
                holder.itemView
                .findViewById<ImageView>(
                        R.id.item_studentlist_student_simple_attendance_trafficlight)
        val attendanceTextView =
                holder.itemView.findViewById<TextView>(
                        R.id.item_studentlist_student_simple_attendance_percentage)
        val cl =
                holder.itemView.findViewById<ConstraintLayout>(
                        R.id.item_studentlist_student_cl)

        val checkBox = holder.itemView.findViewById<CheckBox>(
                R.id.item_studentlist_student_simple_student_checkbox)

        if (groupEnrollment) {
            checkBox.setText(R.string.enroll_group_member)
        } else {
            checkBox.setText(R.string.enroll_in_class)
        }
        val callImageView = holder.itemView.findViewById<ImageView>(
                R.id.item_studentlist_student_simple_call_iv)

        //Update person name :
        var firstName: String? = ""
        var lastName: String? = ""
        if (personWithEnrollment.firstNames != null) {
            firstName = personWithEnrollment.firstNames
        }
        if (personWithEnrollment.lastName != null) {
            lastName = personWithEnrollment.lastName
        }
        val studentName = "$firstName $lastName"
        studentNameTextView.text = studentName

        //Name click listener:
        val personUid = personWithEnrollment.personUid
        studentNameTextView.setOnClickListener { mPresenter!!.handleCommonPressed(personUid) }

        //Remove previous add clazz member views
        if (addStudentsId != 0 || addTeachersId != 0) {
            removeAllAddClazzMemberView(cl, holder)
        }

        //PICTURE : Add picture to person

        var imgPath = ""

        holder.imageLoadJob?.cancel()

        holder.imageLoadJob = GlobalScope.async(Dispatchers.Main) {

            personPictureDaoRepo =
                    UmAccountManager.getRepositoryForActiveAccount(holder.itemView.context).personPictureDao
            val personPictureDao = UmAccountManager.getActiveDatabase(holder.itemView.context).personPictureDao

            val personPictureLocal = personPictureDao.findByPersonUidAsync(personUid)
            imgPath = personPictureDaoRepo!!.getAttachmentPath(personPictureLocal!!)!!

            if (!imgPath!!.isEmpty())
                setPictureOnView(imgPath, personPicture!!)
            else
                personPicture.setImageResource(R.drawable.ic_person_black_new_24dp)

            val personPictureEntity = personPictureDaoRepo!!.findByPersonUidAsync(personUid)
            imgPath = personPictureDaoRepo!!.getAttachmentPath(personPictureEntity!!)!!

            if(personPictureLocal != personPictureEntity) {
                if (!imgPath!!.isEmpty())
                    setPictureOnView(imgPath, personPicture!!)
                else
                    personPicture.setImageResource(R.drawable.ic_person_black_new_24dp)
            }
        }


        //ENROLLMENT
        if (showEnrollment) {
            checkBox.visibility = View.VISIBLE
            checkBox.setTextColor(Color.BLACK)
            checkBox.systemUiVisibility = View.VISIBLE
            checkBox.isCursorVisible = true

            //Get current person's enrollment w.r.t. this class.
            // (Its either set or null (not enrolled))
            val personWithEnrollmentBoolean: Boolean
            if (personWithEnrollment.enrolled != null) {
                personWithEnrollmentBoolean = personWithEnrollment.enrolled!!
            } else {
                personWithEnrollmentBoolean = false
            }

            //To preserve checkboxes, add this enrollment to the Map.
            checkBoxHM[personWithEnrollment.personUid] = personWithEnrollmentBoolean
            //set the value of the check according to the value..
            checkBox.isChecked = checkBoxHM[personWithEnrollment.personUid]!!

            //Add a change listener to the checkbox
            checkBox.setOnCheckedChangeListener {
                buttonView, isChecked -> checkBox.isChecked = isChecked
            }

            checkBox.setOnClickListener { v ->
                val isChecked = checkBox.isChecked
                val arguments = HashMap<PersonWithEnrollment, Boolean>()
                arguments[personWithEnrollment] = isChecked
                mPresenter!!.handleSecondaryPressed(arguments.entries.iterator().next())
            }

        } else {
            //If you want the whole CL to be clickable
            cl.setOnClickListener { v -> mPresenter!!.handleCommonPressed(personUid) }
        }

        if (showAttendance) {
            val attendancePercentage = (personWithEnrollment.attendancePercentage * 100).toLong()

            val attendanceStringLiteral: String
            if (theActivity != null) {
                attendanceStringLiteral = theActivity!!.getText(R.string.attendance).toString()
            } else {
                attendanceStringLiteral = theFragment!!.getText(R.string.attendance).toString()
            }

            val studentAttendancePercentage = attendancePercentage.toString() +
                    "% " + attendanceStringLiteral

            trafficLight.visibility = View.VISIBLE
            if (attendancePercentage > 75L) {
                trafficLight.setColorFilter(ContextCompat.getColor(holder.itemView.context,
                        R.color.traffic_green))
            } else if (attendancePercentage > 50L) {
                trafficLight.setColorFilter(ContextCompat.getColor(holder.itemView.context,
                        R.color.traffic_orange))
            } else {
                trafficLight.setColorFilter(ContextCompat.getColor(holder.itemView.context,
                        R.color.traffic_red))
            }

            attendanceTextView.visibility = View.VISIBLE
            attendanceTextView.text = studentAttendancePercentage
        } else {

            //Change the constraint layout so that the hidden bits are not empty spaces.

            val constraintSet = ConstraintSet()
            constraintSet.clone(cl)

            //connect divider's top to image's bottom
            constraintSet.connect(R.id.item_studentlist_student_simple_horizontal_divider,
                    ConstraintSet.TOP, R.id.item_studentlist_student_simple_student_title,
                    ConstraintSet.BOTTOM, 16)
            constraintSet.connect(R.id.item_studentlist_student_simple_attendance_percentage,
                    ConstraintSet.TOP, R.id.item_studentlist_student_simple_student_title,
                    ConstraintSet.BOTTOM, 0)
            constraintSet.connect(R.id.item_studentlist_student_simple_attendance_trafficlight,
                    ConstraintSet.TOP, R.id.item_studentlist_student_simple_student_title,
                    ConstraintSet.BOTTOM, 0)
            constraintSet.applyTo(cl)

            //or just leave the spaces in hopes of better performance ?
            //Update it doesn't really make it quicker

        }
        if (reportMode) {
            checkBox.visibility = View.GONE

            callImageView.visibility = View.VISIBLE
            callImageView.setOnClickListener {
                v -> mPresenter!!.handleSecondaryPressed(personWithEnrollment)
            }


        } else {
            callImageView.visibility = View.GONE
        }

        //Don't show enrollment and show Attendance - The Clazz Detail Students & Teachers page
        if (!showEnrollment && showAttendance && !reportMode && !hideHeading) {

            val currentRole = personWithEnrollment.clazzMemberRole

            if (position == 0 ) {//First Entry. Add Teacher and Add Teacher item

                addStudentAdded = false
                //Add teacher heading first (always)
                addHeadingAndNew(cl, ClazzMember.ROLE_TEACHER, showAddTeacher, holder)

                when {
                    //First one is a student, add the student heading
                    currentRole == ClazzMember.ROLE_STUDENT ->
                        addHeadingAndNew(cl, ClazzMember.ROLE_STUDENT,showAddStudent, holder)
                    currentRole == ClazzMember.ROLE_TEACHER
                            && position  == itemCount - 1 -> {
                        emptyAddStudents = true
                        addHeadingAndNew(cl, ClazzMember.ROLE_STUDENT, showAddStudent, holder)
                            }
                    else -> {}
                }

            } else {

                val previousPerson = getItem(position - 1)!!
                val previousRole = previousPerson.clazzMemberRole

                if ( previousRole == ClazzMember.ROLE_TEACHER &&
                            currentRole == ClazzMember.ROLE_STUDENT)
                {
                    addHeadingAndNew(cl, ClazzMember.ROLE_STUDENT, showAddStudent, holder)
                }
                //If its teacher still but the last item (ie: Only teachers here)
                else if (currentRole == ClazzMember.ROLE_TEACHER &&
                                        position  == itemCount - 1 )
                {
                    emptyAddStudents = true
                    addHeadingAndNew(cl, ClazzMember.ROLE_STUDENT, showAddStudent, holder)
                }
                else {}
            }
        }

        if (groupByClass) {

            val thisClazzUid = personWithEnrollment.clazzUid
            removeHeading(cl, headingCLId, holder)

            if (position == 0) {
                addHeading(holder.itemView.context, cl, personWithEnrollment.clazzName!!, holder)
            } else {
                val previousPersonWithEnrollment = getItem(position - 1)
                val previousClazzUid = previousPersonWithEnrollment!!.clazzUid

                if (thisClazzUid != previousClazzUid) {
                    addHeading(holder.itemView.context, cl, personWithEnrollment.clazzName!!, holder)
                } else {
                    addHeading(holder.itemView.context, cl, "", holder)
                }

            }
        } else {
            removeHeading(cl, headingCLId, holder)
        }

        if (personWithEnrollment.clazzMemberRole == ClazzMember.ROLE_TEACHER) {

            //Disable attendance for Teachers
            trafficLight.visibility = View.GONE
            attendanceTextView.visibility = View.GONE
        } else {
            //Do nothing;
            val x: Int
        }

    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    private fun setPictureOnView(imagePath: String, theImage: ImageView) {

        val imageUri = Uri.fromFile(File(imagePath))

        Picasso
                .get()
                .load(imageUri)
                .resize(0, dpToPxImagePerson())
                .noFade()
                .into(theImage)
    }

    /**
     * Removes old Add ClazzMember views
     *
     * @param cl    The constraint layout to search in
     * @param holder    The holder that has the itemView
     */
    private fun removeAllAddClazzMemberView(cl: ConstraintLayout,
                                            holder: ClazzLogDetailViewHolder) {

        //Get Clazz Member layout for student and teacher
        val addCMCLViewS = holder.itemView.findViewById<View>(addStudentsId)
        val addCMCLViewT = holder.itemView.findViewById<View>(addTeachersId)

        //Remove the views
        cl.removeView(addCMCLViewS)
        cl.removeView(addCMCLViewT)


        //If view exists, set it to invisible/gone
        if (addCMCLViewS != null) {
            addCMCLViewS.visibility = View.GONE
        }
        if (addCMCLViewT != null) {
            addCMCLViewT.visibility = View.GONE
        }
    }

    /**
     * Removes old Add ClazzMember views
     *
     * @param cl    The constraint layout to search in
     * @param holder    The holder that has the itemView
     */
    private fun removeAddTeacherAddView(cl: ConstraintLayout,
                                        holder: ClazzLogDetailViewHolder) {

        //Get Clazz Member layout for student and teacher
        val addCMCLViewT = holder.itemView.findViewById<View>(addTeachersId)

        //Remove the views
        cl.removeView(addCMCLViewT)

        if (addCMCLViewT != null) {
            addCMCLViewT.visibility = View.GONE
        }
    }

    /**
     * Removes old Add ClazzMember views
     *
     * @param cl    The constraint layout to search in
     * @param holder    The holder that has the itemView
     */
    private fun removeAddStudentView(cl: ConstraintLayout,
                                     holder: ClazzLogDetailViewHolder) {

        //Get Clazz Member layout for student and teacher
        val addCMCLViewS = holder.itemView.findViewById<View>(addStudentsId)

        //Remove the views
        cl.removeView(addCMCLViewS)


        //If view exists, set it to invisible/gone
        if (addCMCLViewS != null) {
            addCMCLViewS.visibility = View.GONE
        }
    }

    private fun removeHeading(cl: ConstraintLayout, headingId: Int,
                              holder: ClazzLogDetailViewHolder) {
        val removeMe = holder.itemView.findViewById<View>(headingId)

        if (removeMe != null)
            removeMe.visibility = View.GONE

        cl.removeView(removeMe)

    }

    /**
     * Adds a heading
     * @param mainCL    The Constraint layout where the list will be in.
     * @param heading  The heading
     */
    private fun addHeading(context: Context, mainCL: ConstraintLayout, heading: String,
                           holder: ClazzLogDetailViewHolder) {


        removeHeading(mainCL, headingCLId, holder)


        if (heading.isEmpty()) {
            return
        }

        val headingCL = ConstraintLayout(context)
        val defaultPaddingBy2 = getDp(context,16 / 2)

        //The Heading TextView
        val headingTV = TextView(context)
        headingTV.setTextColor(Color.BLACK)
        headingTV.textSize = 16f
        headingTV.left = 8

        //Set ids for all components.
        headingCLId = View.generateViewId()
        headingTV.id = View.generateViewId()
        headingCL.setId(headingCLId)

        //Set heading text
        headingTV.text = heading

        //Add these components to the new "add" Constraint Layout
        headingCL.addView(headingTV)

        val constraintSetForHeader2 = ConstraintSet()
        constraintSetForHeader2.clone(headingCL)

        //Heading constraint to parent of the headingCL mainCL it is in.
        constraintSetForHeader2.connect(
                headingTV.id, ConstraintSet.TOP,
                headingCL.getId(), ConstraintSet.TOP,
                defaultPaddingBy2)

        constraintSetForHeader2.connect(
                headingTV.id, ConstraintSet.START,
                headingCL.getId(), ConstraintSet.START,
                defaultPaddingBy2)

        constraintSetForHeader2.applyTo(headingCL)

        //Add the heading CL to the main CL
        mainCL.addView(headingCL)

        val constraintSetForHeader = ConstraintSet()
        constraintSetForHeader.clone(mainCL)

        //Current Person image TOP to BOTTOM of [ Heading CL ] (always)
        constraintSetForHeader.connect(
                R.id.item_studentlist_student_simple_student_image, ConstraintSet.TOP,
                headingCL.getId(), ConstraintSet.BOTTOM, defaultPaddingBy2)

        //Current Person title TOP to BOTTOM of [ Add CL ] (always)
        constraintSetForHeader.connect(
                R.id.item_studentlist_student_simple_student_title, ConstraintSet.TOP,
                headingCL.getId(), ConstraintSet.BOTTOM, defaultPaddingBy2)

        constraintSetForHeader.applyTo(mainCL)


    }

    /**
     * Adds a heading depending on roleToAdd given
     * @param personCL    The Constraint layout where the list will be in.
     * @param roleToAdd  The roleToAdd (Teacher or Student) as per ClazzMember.ROLE_*
     */
    private fun addHeadingAndNew(personCL: ConstraintLayout, roleToAdd: Int, showAdd: Boolean,
                                 holder: ClazzLogDetailViewHolder) {

        //Remove any previous views
        if (roleToAdd == ClazzMember.ROLE_TEACHER) {
            removeAddTeacherAddView(personCL, holder)
        } else {
            removeAddStudentView(personCL, holder)
        }

        //Create the Constraint layout wrapper, the heading, icon and button
        val addCL = ConstraintLayout(holder.itemView.context)
        val defaultPadding = getDp(holder.itemView.context, 16)
        val defaultPaddingBy2 = getDp(holder.itemView.context, 16 / 2)

        //"Teachers/Students" heading
        val headingTV = TextView(holder.itemView.context)
        headingTV.setTextColor(Color.BLACK)
        headingTV.textSize = 16f
        headingTV.left = 8

        //Add person icon
        val iconIV = AppCompatImageView(holder.itemView.context)
        iconIV.setImageResource(addPersonIconRes)

        //"Add Student/Teacher"
        val buttonTV = TextView(holder.itemView.context)
        buttonTV.setTextColor(Color.BLACK)
        buttonTV.textSize = 16f
        buttonTV.left = 8

        //Horizontal line
        val hLine = View(holder.itemView.context)
        hLine.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                getDp(holder.itemView.context, 1))
        hLine.setBackgroundColor(Color.parseColor("#EAEAEA"))

        //Get ids for all components.
        val headingId = View.generateViewId()
        val iconId = View.generateViewId()
        val buttonId = View.generateViewId()
        val hLineId = View.generateViewId()
        val addCLId = View.generateViewId()

        //Set ids for all components.
        headingTV.id = headingId
        buttonTV.id = buttonId
        hLine.id = hLineId
        iconIV.setId(iconId)
        addCL.setId(addCLId)

        //Add these components to the new Constraint Layout
        addCL.addView(headingTV)
        if (roleToAdd == ClazzMember.ROLE_STUDENT && showAddStudent) {
            addCL.addView(iconIV)
            if (showAdd)
                addCL.addView(buttonTV)
            else {
                val blankView = View(holder.itemView.context)
                blankView.visibility = View.GONE
                addCL.addView(blankView)
            }
            addCL.addView(hLine)
        }
        if (roleToAdd == ClazzMember.ROLE_TEACHER && showAddTeacher) {
            addCL.addView(iconIV)
            if (showAdd)
                addCL.addView(buttonTV)
            else {
                val blankView = View(holder.itemView.context)
                blankView.visibility = View.GONE
                addCL.addView(blankView)
            }
            addCL.addView(hLine)
        }


        //Create the constraint set for the headingCL View
        val constraintSet = ConstraintSet()
        constraintSet.clone(addCL)

        /**
         * [Teachers / Students Heading] TOP to TOP of whichever the top
         * (can be Parent or horizontal line of previous item)
         * +----*-------------------+   <headingCL>
         * | Teachers   <headingId> |
         * |                        |
         * |                        |
         * +________________________+
         */
        constraintSet.connect(
                headingId, ConstraintSet.TOP,
                addCL.getId(), ConstraintSet.TOP,
                defaultPaddingBy2)

        /**
         * [Teachers / Students Heading] START to START of PARENT (always)
         * +------------------------+   <headingCL>
         * |*Teachers   <headingId> |
         * |                        |
         * |                        |
         * +________________________+
         */
        constraintSet.connect(
                headingId, ConstraintSet.START,
                addCL.getId(), ConstraintSet.START,
                defaultPaddingBy2)

        /**
         * [Add teacher/student Icon] START to START of Parent (always)
         * +------------------------+   <headingCL>
         * | Teachers               |
         * |*[+]        <iconId>    |
         * |                        |
         * +________________________+
         */
        constraintSet.connect(
                iconId, ConstraintSet.START,
                addCL.getId(), ConstraintSet.START, defaultPadding)

        /**
         * [Add teacher/student Icon] TOP to BOTTOM of Heading
         * +------------------------+
         * | Teachers   <headingId> |
         * | *                      |
         * |[+]        <iconId>     |
         * |                        |
         * +________________________+
         */
        constraintSet.connect(
                iconId, ConstraintSet.TOP,
                headingId, ConstraintSet.BOTTOM, defaultPaddingBy2)

        /**
         * [Add teacher/student Text]  START to Icon END (always)
         * +----------------------------+
         * | Teachers                   |
         * |                            |
         * | <iconId>                   |
         * |[+]*Add Teacher <buttonId>  |
         * |                            |
         * +____________________________+
         */
        constraintSet.connect(
                buttonId, ConstraintSet.START,
                iconId, ConstraintSet.END, defaultPadding)

        /**
         * [Add teacher/student Text] TOP to [Teacher / Students Heading] Bottom (always)
         * +----------------------------+
         * | Teachers       <headingId> |
         * |    *                       |
         * |[+]Add Teacher   <buttonId> |
         * |                            |
         * |                            |
         * +____________________________+
         */
        constraintSet.connect(
                buttonId, ConstraintSet.TOP,
                headingId, ConstraintSet.BOTTOM, defaultPaddingBy2)

        /**
         * [Add Teacher/Student HLine] TOP to [Teacher / Student Icon] BOTTOM (always)
         * +----------------------------+
         * | Teachers                   |
         * |[+]Add Teacher <buttonId>   |
         * | *                          |
         * |____________________________| <hLineId>
         * |____________________________+
         */
        constraintSet.connect(
                hLineId, ConstraintSet.TOP,
                iconId, ConstraintSet.BOTTOM, defaultPaddingBy2)

        /**
         * [Add Teacher/Student HLine] START to Parent (always)
         * +----------------------------+
         * | Teachers                   |
         * |[+]Add Teacher <buttonId>   |
         * |                            |
         * |*___________________________| <hLineId>
         * +____________________________+
         */
        constraintSet.connect(
                hLineId, ConstraintSet.START,
                addCL.getId(), ConstraintSet.START, 0)

        constraintSet.applyTo(addCL)

        if(addStudentAdded){
            personCL.addView(addCL)
        }else{
            personCL.addView(addCL)
        }

        val personCS = ConstraintSet()
        personCS.clone(personCL)

        //Set strings and handler on the components based on roleToAdd.
        if (roleToAdd == ClazzMember.ROLE_STUDENT) {
            buttonTV.text = getText(R.string.add_student)
            headingTV.text = getText(R.string.students_literal)
            addCL.setOnClickListener({ v -> mPresenter!!.handleCommonPressed(0L) })

            //Storing in separate variables so we can remove them.
            addStudentsId = addCL.id

        } else {

            buttonTV.text = getText(R.string.add_teacher)
            headingTV.text = getText(R.string.teachers_literal)
            addCL.setOnClickListener({ v -> mPresenter!!.handleSecondaryPressed(-1L) })

            //Storing in separate variables so we can remove them.
            addTeachersId = addCL.id

            //For Teachers (which will always be the start of the recycler view: we keep the top as
            // the top of the whole inner constraint layout .
            if(!emptyAddStudents){
                currentTop = addCL.id
            }

        }

        if (addTeacherAdded) {
            //"Add Students" bit goes On TOP of Teacher.

            /**
             * [ Add CL [Students] ] TOP to BOTTOM of Current Top
             *              *
             * +----------------------------+ <headingCL>
             * | Students                   |
             * |[+]Add Student              |
             * |----------------------------|
             * |[o] Bob Ross                |
             * +____________________________+
             */
            personCS.connect(
                    addCL.id, ConstraintSet.TOP,
                    currentTop, ConstraintSet.BOTTOM, defaultPaddingBy2)

            /**
             * [ Add CL [Students] ] always start to start parent (always)
             * *
             * +----------------------------+ <headingCL>
             * | Students                   |
             * |[+]Add Student              |
             * |----------------------------|
             * |[o] Bob Ross                |
             * +____________________________+
             */
            personCS.connect(
                    addCL.id, ConstraintSet.START,
                    personCL.id, ConstraintSet.START, defaultPaddingBy2)


            if(!emptyAddStudents) {

                //Current Person image TOP to BOTTOM of [ Add CL ] (always)
                personCS.connect(
                        R.id.item_studentlist_student_simple_student_image, ConstraintSet.TOP,
                        addCL.getId(), ConstraintSet.BOTTOM,
                         defaultPaddingBy2)

                //Current Person title TOP to BOTTOM of [ Add CL ] (always)
                personCS.connect(
                        R.id.item_studentlist_student_simple_student_title, ConstraintSet.TOP,
                        addCL.getId(), ConstraintSet.BOTTOM,
                         defaultPaddingBy2)

            }else{

                var top = addCL.id


                //Current Person image TOP to BOTTOM of [ Add CL ] (always)
                personCS.connect(top, ConstraintSet.TOP,
                        R.id.item_studentlist_student_simple_student_title, ConstraintSet.BOTTOM,
                        defaultPaddingBy2)

                //Current Person image TOP to BOTTOM of [ Add CL ] (always)
                personCS.connect(top, ConstraintSet.TOP,
                        R.id.item_studentlist_student_simple_student_image, ConstraintSet.BOTTOM,
                        defaultPaddingBy2)
            }

            personCS.applyTo(personCL)


            //Update the top for the next
            currentTop = addCL.getId()

        } else {

            /**
             * [Add CL Teacher ] always on top to top parent.
             *              *
             * +----------------------------+ <headingCL>
             * | Teachers                   |
             * |[+]Add Teacher              |
             * |----------------------------|
             * |[o] Bob Dylan               |
             * +____________________________+
             */
            personCS.connect(
                    addCL.getId(), ConstraintSet.TOP,
                    personCL.getId(), ConstraintSet.TOP, defaultPaddingBy2)

            /**
             * [ Add CL [Teachers] ] always start to start parent (always)
             * *
             * +----------------------------+ <headingCL>
             * | Teachers                   |
             * |[+]Add Teacher              |
             * |----------------------------|
             * |[o] Bob Dylan               |
             * +____________________________+
             */
            personCS.connect(
                    addCL.getId(), ConstraintSet.START,
                    personCL.getId(), ConstraintSet.START, defaultPaddingBy2)

            //Current Person image TOP to BOTTOM of [ Add CL ] (always)
            personCS.connect(
                    R.id.item_studentlist_student_simple_student_image, ConstraintSet.TOP,
                    addCL.getId(), ConstraintSet.BOTTOM, defaultPaddingBy2)

            //Current Person title TOP to BOTTOM of [ Add CL ] (always)
            personCS.connect(
                    R.id.item_studentlist_student_simple_student_title, ConstraintSet.TOP,
                    addCL.getId(), ConstraintSet.BOTTOM, defaultPaddingBy2)

            personCS.applyTo(personCL)

            //Update the top for the next
            currentTop = addCL.id

            currentDown = personCL.id
        }

        if (roleToAdd == ClazzMember.ROLE_TEACHER) {
            addTeacherAdded = true
            addStudentAdded = false
        } else {
            addTeacherAdded = false
            addStudentAdded = true
        }

    }


    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        theActivity = null
        theFragment = null
        mPresenter = null
        personPictureDaoRepo = null
    }

    companion object {

        private val IMAGE_PERSON_THUMBNAIL_WIDTH = 26


        private fun dpToPxImagePerson(): Int {
            return (IMAGE_PERSON_THUMBNAIL_WIDTH *
                    Resources.getSystem().displayMetrics.density).toInt()
        }
    }

}