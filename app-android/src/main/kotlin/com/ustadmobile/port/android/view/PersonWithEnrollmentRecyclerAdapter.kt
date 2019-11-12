package com.ustadmobile.port.android.view

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
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.PersonPictureDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.lib.db.entities.ClazzMember
import com.ustadmobile.lib.db.entities.PersonWithEnrollment
import com.ustadmobile.port.android.view.PersonEditActivity.Companion.DEFAULT_PADDING
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

    private var theContext: Context
    private var theActivity: Activity? = null
    private var theFragment: Fragment? = null
    private var mPresenter: CommonHandlerPresenter<*>? = null
    private var showAttendance: Boolean = false
    private var showEnrollment: Boolean = false
    private var groupEnrollment = false

    private var showAddStudent = false
    private var showAddTeacher = false

    private var currentTop = -1
    private var teacherAdded = false

    private var reportMode = false

    private var groupByClass = false

    private var hideHeading = false

    private var headingCLId: Int = 0

    private var addCMCLT: Int = 0
    private var addCMCLS: Int = 0

    private var personPictureDaoRepo: PersonPictureDao?=null

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

    class ClazzLogDetailViewHolder(itemView: View, var imageLoadJob: Job? = null) : RecyclerView.ViewHolder(itemView)

    internal constructor(
            diffCallback: DiffUtil.ItemCallback<PersonWithEnrollment>, context: Context,
            activity: Activity, presenter: CommonHandlerPresenter<*>, attendance: Boolean,
            enrollment: Boolean, enrollToGroup: Boolean) : super(diffCallback) {
        theContext = context
        theActivity = activity
        mPresenter = presenter
        showAttendance = attendance
        showEnrollment = enrollment
        groupEnrollment = enrollToGroup
    }

    internal constructor(
            diffCallback: DiffUtil.ItemCallback<PersonWithEnrollment>, context: Context,
            activity: Activity, presenter: CommonHandlerPresenter<*>, attendance: Boolean,
            enrollment: Boolean) : super(diffCallback) {
        theContext = context
        theActivity = activity
        mPresenter = presenter
        showAttendance = attendance
        showEnrollment = enrollment
    }

    internal constructor(
            diffCallback: DiffUtil.ItemCallback<PersonWithEnrollment>, context: Context,
            fragment: Fragment, presenter: CommonHandlerPresenter<*>, attendance: Boolean,
            enrollment: Boolean) : super(diffCallback) {
        theContext = context
        theFragment = fragment
        mPresenter = presenter
        showAttendance = attendance
        showEnrollment = enrollment
    }

    internal constructor(
            diffCallback: DiffUtil.ItemCallback<PersonWithEnrollment>, context: Context,
            activity: Activity, presenter: CommonHandlerPresenter<*>, attendance: Boolean,
            enrollment: Boolean, rmode: Boolean, classGrouped: Boolean) : super(diffCallback) {
        theContext = context
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
            enrollment: Boolean, rmode: Boolean, classGrouped: Boolean, hideHeading: Boolean) : super(diffCallback) {
        theContext = context
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

        val clazzLogDetailListItem = LayoutInflater.from(theContext).inflate(
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
        try {
            return if (theActivity != null) {
                theActivity!!.resources.getIdentifier(PersonEditActivity.ADD_PERSON_ICON,
                        "drawable", pPackageName)
            } else {
                theFragment!!.resources.getIdentifier(PersonEditActivity.ADD_PERSON_ICON,
                        "drawable", pPackageName)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return -1
        }

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
    private fun getDp(dp: Int): Int {
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

        //Flag that says that complete recyclerview has only 1 item and its a teacher. Used to add
        // "Add Students" as well before we finish onBindView for the teacher.
        var onlyTeacherExists = false

        val studentNameTextView = holder.itemView.findViewById<TextView>(R.id.item_studentlist_student_simple_student_title)
        val personPicture = holder.itemView.findViewById<ImageView>(R.id.item_studentlist_student_simple_student_image)
        val trafficLight = holder.itemView
                .findViewById<ImageView>(R.id.item_studentlist_student_simple_attendance_trafficlight)
        val attendanceTextView = holder.itemView.findViewById<TextView>(R.id.item_studentlist_student_simple_attendance_percentage)
        val cl = holder.itemView.findViewById<ConstraintLayout>(R.id.item_studentlist_student_cl)

        val checkBox = holder.itemView.findViewById<CheckBox>(R.id.item_studentlist_student_simple_student_checkbox)
        if (groupEnrollment) {
            checkBox.setText(R.string.enroll_group_member)
        } else {
            checkBox.setText(R.string.enroll_in_class)
        }
        val callImageView = holder.itemView.findViewById<ImageView>(R.id.item_studentlist_student_simple_call_iv)

        //Update person name :
        var firstName: String? = ""
        var lastName: String? = ""
        if (personWithEnrollment == null) {
            return
        }
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
        studentNameTextView.setOnClickListener { v -> mPresenter!!.handleCommonPressed(personUid) }

        //HEADING:
        //Remove previous add clazz member views
        if (addCMCLS != 0 || addCMCLT != 0) {
            removeAllAddClazzMemberView(cl, holder)
        }

        //PICTURE : Add picture to person

        var imgPath = ""

        holder.imageLoadJob?.cancel()

        holder.imageLoadJob = GlobalScope.async(Dispatchers.Main) {

            personPictureDaoRepo = UmAccountManager.getRepositoryForActiveAccount(theContext).personPictureDao
            val personPictureDao = UmAppDatabase.getInstance(theContext).personPictureDao

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
            checkBox.setOnCheckedChangeListener { buttonView, isChecked -> checkBox.isChecked = isChecked }

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
                trafficLight.setColorFilter(ContextCompat.getColor(theContext,
                        R.color.traffic_green))
            } else if (attendancePercentage > 50L) {
                trafficLight.setColorFilter(ContextCompat.getColor(theContext,
                        R.color.traffic_orange))
            } else {
                trafficLight.setColorFilter(ContextCompat.getColor(theContext,
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
            callImageView.setOnClickListener { v -> mPresenter!!.handleSecondaryPressed(personWithEnrollment) }


        } else {
            callImageView.visibility = View.GONE
        }

        if (!showEnrollment && showAttendance) {

            if (position == 0) {//First Entry. Add Teacher and Add Teacher item
                if (!reportMode && !hideHeading) {
                    addHeadingAndNew(cl, ClazzMember.ROLE_TEACHER, showAddTeacher, holder)
                } else {
                    val x: Int
                }

                if (personWithEnrollment.clazzMemberRole == ClazzMember.ROLE_STUDENT) {

                    if (!reportMode && !hideHeading) {
                        addHeadingAndNew(cl, ClazzMember.ROLE_STUDENT, showAddStudent, holder)
                    } else {
                        val x: Int
                    }

                } else {
                    val x: Int
                }

                //If first item is a teacher and there are no more items:
                if (personWithEnrollment.clazzMemberRole == ClazzMember.ROLE_TEACHER && itemCount == 1) {
                    onlyTeacherExists = true
                } else {
                    val x: Int
                }

            } else {
                val previousPerson = getItem(position - 1)!!

                if (previousPerson.clazzMemberRole == ClazzMember.ROLE_TEACHER && personWithEnrollment.clazzMemberRole == ClazzMember.ROLE_STUDENT) {

                    //Add student
                    if (!reportMode && !hideHeading) {
                        addHeadingAndNew(cl, ClazzMember.ROLE_STUDENT, showAddStudent, holder)
                    } else {
                        val x: Int
                    }
                } else {
                    val x: Int
                }

            }

        }

        if (groupByClass) {

            val thisClazzUid = personWithEnrollment.clazzUid
            removeHeading(cl, headingCLId, holder)

            if (position == 0) {
                addHeading(cl, personWithEnrollment.clazzName!!, holder)
            } else {
                val previousPersonWithEnrollment = getItem(position - 1)
                val previousClazzUid = previousPersonWithEnrollment!!.clazzUid

                if (thisClazzUid != previousClazzUid) {
                    addHeading(cl, personWithEnrollment.clazzName!!, holder)
                } else {
                    addHeading(cl, "", holder)
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

        //If we reached the end of the rv and there is only one teacher in it,
        // add the "show Student" as well.
        if (onlyTeacherExists) {
            addHeadingAndNew(cl, ClazzMember.ROLE_STUDENT, showAddStudent, holder)
        } else {
            //Don't add anything.
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
        val addCMCLViewS = holder.itemView.findViewById<View>(addCMCLS)
        val addCMCLViewT = holder.itemView.findViewById<View>(addCMCLT)

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
        val addCMCLViewT = holder.itemView.findViewById<View>(addCMCLT)

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
        val addCMCLViewS = holder.itemView.findViewById<View>(addCMCLS)

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
    private fun addHeading(mainCL: ConstraintLayout, heading: String,
                           holder: ClazzLogDetailViewHolder) {


        removeHeading(mainCL, headingCLId, holder)


        if (heading.isEmpty()) {
            return
        }

        val headingCL = ConstraintLayout(theContext)
        val defaultPaddingBy2 = getDp(DEFAULT_PADDING / 2)

        //The Heading TextView
        val headingTV = TextView(theContext)
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
     * Adds a heading depending on role given
     * @param cl    The Constraint layout where the list will be in.
     * @param role  The role (Teacher or Student) as per ClazzMember.ROLE_*
     */
    private fun addHeadingAndNew(cl: ConstraintLayout, role: Int, showAdd: Boolean,
                                 holder: ClazzLogDetailViewHolder) {

        //Testing if improves:
        if (role == ClazzMember.ROLE_TEACHER) {
            removeAddStudentView(cl, holder)
        } else {
            removeAddTeacherAddView(cl, holder)
        }

        val addCl = ConstraintLayout(theContext)
        val defaultPadding = getDp(DEFAULT_PADDING)
        val defaultPaddingBy2 = getDp(DEFAULT_PADDING / 2)

        val clazzMemberRoleHeadingTextView = TextView(theContext)
        clazzMemberRoleHeadingTextView.setTextColor(Color.BLACK)
        clazzMemberRoleHeadingTextView.textSize = 16f
        clazzMemberRoleHeadingTextView.left = 8

        val addIconResId = addPersonIconRes
        val addPersonImageView = AppCompatImageView(theContext)
        addPersonImageView.setImageResource(addIconResId)

        val addClazzMemberTextView = TextView(theContext)
        addClazzMemberTextView.setTextColor(Color.BLACK)
        addClazzMemberTextView.textSize = 16f
        addClazzMemberTextView.left = 8

        //Horizontal line
        val horizontalLine = View(theContext)
        horizontalLine.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                getDp(1)
        )
        horizontalLine.setBackgroundColor(Color.parseColor("#EAEAEA"))

        //Get ids for all components.
        val headingId = View.generateViewId()
        val headingImageId = View.generateViewId()
        val addClazzMemberId = View.generateViewId()
        val hLineId = View.generateViewId()
        val addCMCL = View.generateViewId()


        //Set strings and handler on the components based on role.
        if (role == ClazzMember.ROLE_STUDENT) {
            addClazzMemberTextView.text = getText(R.string.add_student)
            clazzMemberRoleHeadingTextView.text = getText(R.string.students_literal)
            addCl.setOnClickListener({ v -> mPresenter!!.handleCommonPressed(0L) })

            //Storing in separate variables so we can remove them.
            addCMCLS = addCMCL

        } else {

            teacherAdded = false

            addClazzMemberTextView.text = getText(R.string.add_teacher)
            clazzMemberRoleHeadingTextView.text = getText(R.string.teachers_literal)
            addCl.setOnClickListener({ v -> mPresenter!!.handleSecondaryPressed(-1L) })

            //Storing in separate variables so we can remove them.
            addCMCLT = addCMCL

            //For Teachers (which will always be the start of the recycler view: we keep the top as
            // the top of the whole inner constraint layout .
            currentTop = addCMCL
        }

        //Set ids for all components.
        clazzMemberRoleHeadingTextView.id = headingId
        addClazzMemberTextView.id = addClazzMemberId
        horizontalLine.id = hLineId
        addPersonImageView.setId(headingImageId)
        addCl.setId(addCMCL)

        //Add these components to the new "add" Constraint Layout
        addCl.addView(clazzMemberRoleHeadingTextView)
        if (role == ClazzMember.ROLE_STUDENT && showAddStudent) {
            addCl.addView(addPersonImageView)
            if (showAdd)
                addCl.addView(addClazzMemberTextView)
            else {
                val blankView = View(theContext)
                blankView.visibility = View.GONE
                addCl.addView(blankView)
            }
            addCl.addView(horizontalLine)
        }
        if (role == ClazzMember.ROLE_TEACHER && showAddTeacher) {
            addCl.addView(addPersonImageView)
            if (showAdd)
                addCl.addView(addClazzMemberTextView)
            else {
                val blankView = View(theContext)
                blankView.visibility = View.GONE
                addCl.addView(blankView)

            }

            addCl.addView(horizontalLine)
        }


        val constraintSetForHeader2 = ConstraintSet()
        constraintSetForHeader2.clone(addCl)

        //[Teachers / Students Heading] TOP to TOP of whichever the top
        //  (can be Parent or horizontal line of previous item)
        constraintSetForHeader2.connect(
                headingId, ConstraintSet.TOP,
                addCl.getId(), ConstraintSet.TOP,
                defaultPaddingBy2)

        //[Teachers / Students Heading] START to START of PARENT (always)
        constraintSetForHeader2.connect(
                headingId, ConstraintSet.START,
                addCl.getId(), ConstraintSet.START,
                defaultPaddingBy2)

        //[Add teacher/student Icon] START to START of Parent (always)
        constraintSetForHeader2.connect(
                headingImageId, ConstraintSet.START,
                addCl.getId(), ConstraintSet.START, defaultPadding)

        //[Add teacher/student Icon] TOP to BOTTOM of Heading
        constraintSetForHeader2.connect(
                headingImageId, ConstraintSet.TOP,
                headingId, ConstraintSet.BOTTOM, defaultPaddingBy2)

        //[Add teacher/student Text]  START to Icon END (always)
        constraintSetForHeader2.connect(
                addClazzMemberId, ConstraintSet.START,
                headingImageId, ConstraintSet.END, defaultPadding)

        //[Add teacher/student Text] TOP to [Teacher / Students Heading] Bottom (always)
        constraintSetForHeader2.connect(
                addClazzMemberId, ConstraintSet.TOP,
                headingId, ConstraintSet.BOTTOM, defaultPaddingBy2)

        //[Add Teacher/Student HLine] TOP to [Teacher / Student Icon] BOTTOM (always)
        constraintSetForHeader2.connect(
                hLineId, ConstraintSet.TOP,
                headingImageId, ConstraintSet.BOTTOM, defaultPaddingBy2)

        //[Add Teacher/Student HLine] START to Parent (always)
        constraintSetForHeader2.connect(hLineId, ConstraintSet.START,
                addCl.getId(), ConstraintSet.START, 0)

        //Current Person image TOP to BOTTOM of horizontal line (always)
        constraintSetForHeader2.connect(
                R.id.item_studentlist_student_simple_student_image, ConstraintSet.TOP,
                hLineId, ConstraintSet.BOTTOM, defaultPaddingBy2)

        //Current Person title TOP to BOTTOM of horizontal line (always)
        constraintSetForHeader2.connect(
                R.id.item_studentlist_student_simple_student_title, ConstraintSet.TOP,
                hLineId, ConstraintSet.BOTTOM, defaultPaddingBy2)

        constraintSetForHeader2.applyTo(addCl)

        cl.addView(addCl)

        val constraintSetForHeader = ConstraintSet()
        constraintSetForHeader.clone(cl)


        if (teacherAdded) {

            // [Add CL Student ] always on top to top parent.
            constraintSetForHeader.connect(
                    addCl.getId(), ConstraintSet.TOP,
                    currentTop, ConstraintSet.BOTTOM, defaultPaddingBy2)

            // [ Add CL [Teachers] ] always start to start parent.
            constraintSetForHeader.connect(
                    addCl.getId(), ConstraintSet.START,
                    cl.getId(), ConstraintSet.START, defaultPaddingBy2)

        } else {

            // [Add CL Teacher ] always on top to top parent.
            constraintSetForHeader.connect(
                    addCl.getId(), ConstraintSet.TOP,
                    cl.getId(), ConstraintSet.TOP, defaultPaddingBy2)

            // [ Add CL [Teachers] ] always start to start parent.
            constraintSetForHeader.connect(
                    addCl.getId(), ConstraintSet.START,
                    cl.getId(), ConstraintSet.START, defaultPaddingBy2)
        }

        //Current Person image TOP to BOTTOM of [ Add CL ] (always)
        constraintSetForHeader.connect(
                R.id.item_studentlist_student_simple_student_image, ConstraintSet.TOP,
                addCl.getId(), ConstraintSet.BOTTOM, defaultPaddingBy2)

        //Current Person title TOP to BOTTOM of [ Add CL ] (always)
        constraintSetForHeader.connect(
                R.id.item_studentlist_student_simple_student_title, ConstraintSet.TOP,
                addCl.getId(), ConstraintSet.BOTTOM, defaultPaddingBy2)


        constraintSetForHeader.applyTo(cl)

        //Update the top for the next
        currentTop = addCl.getId()

        if (role == ClazzMember.ROLE_TEACHER) {
            teacherAdded = true
        } else {
            teacherAdded = false
        }

    }

    companion object {

        private val IMAGE_PERSON_THUMBNAIL_WIDTH = 26


        private fun dpToPxImagePerson(): Int {
            return (PersonWithEnrollmentRecyclerAdapter.IMAGE_PERSON_THUMBNAIL_WIDTH * Resources.getSystem().displayMetrics.density).toInt()
        }
    }

}