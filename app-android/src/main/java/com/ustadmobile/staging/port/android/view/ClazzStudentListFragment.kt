package com.ustadmobile.staging.port.android.view

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.ClazzStudentListPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.ClazzListView.Companion.ARG_CLAZZ_UID
import com.ustadmobile.core.view.ClazzStudentListView
import com.ustadmobile.lib.db.entities.ClazzMember
import com.ustadmobile.lib.db.entities.PersonWithEnrollment
import com.ustadmobile.staging.port.android.view.PersonEditActivity.Companion.DEFAULT_PADDING
import com.ustadmobile.port.android.view.UstadBaseFragment

/**
 * ClazzStudentListFragment Android fragment extends UstadBaseFragment
 */
class ClazzStudentListFragment : UstadBaseFragment(), ClazzStudentListView {

    override val viewContext: Any
        get() = context!!

    internal lateinit var rootContainer: View
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mPresenter: ClazzStudentListPresenter
    private lateinit var sortSpinner: Spinner
    internal lateinit var sortSpinnerPresets: Array<String?>
    private lateinit var cl: ConstraintLayout
    private var addClazzMemberEmptyAdded: Boolean = false


    private var addCMCLT: Int = 0
    private var addCMCLS: Int = 0
    internal var teacherAdded: Boolean? = false
    private var currentTop = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        rootContainer = inflater.inflate(R.layout.fragment_class_student_list, container, false)
        setHasOptionsMenu(true)


        cl = rootContainer.findViewById(R.id.fragment_class_student_list_cl)

        mRecyclerView = rootContainer.findViewById(R.id.fragment_class_student_list_recyclerview)
        val mRecyclerLayoutManager = LinearLayoutManager(context)
        mRecyclerView.layoutManager = mRecyclerLayoutManager


        //Sort Fragment:
        sortSpinner = rootContainer.findViewById(R.id.fragment_class_student_list_sort_spinner)

        //Reset created flags
        addClazzMemberEmptyAdded = false

        //Create the presenter and call its onCreate method. This will populate the provider data
        // and call setProvider to set it
        mPresenter = ClazzStudentListPresenter(context!!,
                UMAndroidUtil.bundleToMap(arguments), this)
        mPresenter.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        //Sort handler
        sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                mPresenter.handleChangeSortOrder(id)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        return rootContainer
    }

    override fun setPersonWithEnrollmentProvider(factory: DataSource.Factory<Int, PersonWithEnrollment>) {

        val recyclerAdapter = PersonWithEnrollmentRecyclerAdapter(DIFF_CALLBACK2, context!!,
                this, mPresenter, true, false,
                mPresenter.isCanAddTeachers, mPresenter.isCanAddStudents)


        //A warning is expected

        val data = LivePagedListBuilder(factory, 20).build()

        val customObserver = Observer { o: PagedList<PersonWithEnrollment> ->

            if (o.size === 0) {
                if (!addClazzMemberEmptyAdded) {
                    removeAddClazzMemberHeadings()
                    addAddClazzMemberHeadings()
                    addClazzMemberEmptyAdded = true
                }
            } else {
                if (addClazzMemberEmptyAdded) {
                    removeAddClazzMemberHeadings()
                    addClazzMemberEmptyAdded = false
                }
            }
            recyclerAdapter.submitList(o)
        }

        data.observe(this, customObserver)
        mRecyclerView.adapter = recyclerAdapter
    }

    /**
     * Removes old Add ClazzMember views
     *
     */
    private fun removeAddClazzMemberHeadings() {

        //Get Clazz Member layout for student and teacher
        val addCMCLViewS = rootContainer.findViewById<View>(addCMCLS)
        val addCMCLViewT = rootContainer.findViewById<View>(addCMCLT)

        //Remove the views
        cl.removeView(addCMCLViewS)
        cl.removeView(addCMCLViewT)

        //If view exists, set it to invisible
        if (addCMCLViewS != null) {
            addCMCLViewS.visibility = View.INVISIBLE
        }
        if (addCMCLViewT != null) {
            addCMCLViewT.visibility = View.INVISIBLE
        }
    }

    /**
     * Add new "Add Student/Teacher" headings
     */
    private fun addAddClazzMemberHeadings() {
        if (mPresenter.isTeachersEditable) {
            addHeadingAndNew(cl, ClazzMember.ROLE_TEACHER, context)
        }

        addHeadingAndNew(cl, ClazzMember.ROLE_STUDENT, context)
    }

    override fun updateSortSpinner(presets: Array<String?>) {
        this.sortSpinnerPresets = presets
        val adapter = ArrayAdapter(context!!,
                R.layout.spinner_item, sortSpinnerPresets)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sortSpinner.adapter = adapter
    }


    /**
     * Get dp from pixel
     *
     * @param dp the pixels
     * @return  The dp
     */
    private fun getDp(dp: Int): Int {

        return Math.round(
                dp * resources.displayMetrics.density)
    }

    /**
     * Adds a heading depending on role given
     * @param cl    The Constraint layout where the list will be in.
     * @param role  The role (Teacher or Student) as per ClazzMember.ROLE_*
     */
    private fun addHeadingAndNew(cl: ConstraintLayout?, role: Int, mContext: Context?) {

        val addCl = ConstraintLayout(mContext)
        val defaultPadding = getDp(DEFAULT_PADDING)

        val defaultPaddingBy2 = getDp(DEFAULT_PADDING / 2)

        val clazzMemberRoleHeadingTextView = TextView(mContext)
        clazzMemberRoleHeadingTextView.setTextColor(Color.BLACK)
        clazzMemberRoleHeadingTextView.textSize = 16f
        clazzMemberRoleHeadingTextView.left = 8

        val addIconResId = resources.getIdentifier(PersonEditActivity.ADD_PERSON_ICON,
                "drawable", activity!!.packageName)

        val addPersonImageView = ImageView(mContext)
        addPersonImageView.setImageResource(addIconResId)

        val addClazzMemberTextView = TextView(mContext)
        addClazzMemberTextView.setTextColor(Color.BLACK)
        addClazzMemberTextView.textSize = 16f
        addClazzMemberTextView.left = 8

        //Horizontal line
        val horizontalLine = View(mContext)
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
            addCl.setOnClickListener { v -> mPresenter!!.handleCommonPressed(0L) }

            //Storing in separate variables so we can remove them.
            addCMCLS = addCMCL

        } else {

            teacherAdded = false

            addClazzMemberTextView.text = getText(R.string.add_teacher)
            clazzMemberRoleHeadingTextView.text = getText(R.string.teachers_literal)
            addCl.setOnClickListener { v -> mPresenter.handleSecondaryPressed(-1L) }

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
        addPersonImageView.id = headingImageId
        addCl.id = addCMCL

        //Add these components to the new "add" Constraint Layout
        addCl.addView(clazzMemberRoleHeadingTextView)
        addCl.addView(addPersonImageView)
        addCl.addView(addClazzMemberTextView)
        addCl.addView(horizontalLine)

        val constraintSetForHeader2 = ConstraintSet()
        constraintSetForHeader2.clone(addCl)

        //[Teachers / Students Heading] TOP to TOP of whichever the top
        //  (can be Parent or horizontal line of previous item)
        constraintSetForHeader2.connect(
                headingId, ConstraintSet.TOP,
                addCl.id, ConstraintSet.TOP,
                defaultPaddingBy2)

        //[Teachers / Students Heading] START to START of PARENT (always)
        constraintSetForHeader2.connect(
                headingId, ConstraintSet.START,
                addCl.id, ConstraintSet.START,
                defaultPaddingBy2)

        //[Add teacher/student Icon] START to START of Parent (always)
        constraintSetForHeader2.connect(
                headingImageId, ConstraintSet.START,
                addCl.id, ConstraintSet.START, defaultPadding)

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
                addCl.id, ConstraintSet.START, 0)

        //Current Person image TOP to BOTTOM of horizontal line (always)
        constraintSetForHeader2.connect(
                R.id.item_studentlist_student_simple_student_image, ConstraintSet.TOP,
                hLineId, ConstraintSet.BOTTOM, defaultPaddingBy2)

        //Current Person title TOP to BOTTOM of horizontal line (always)
        constraintSetForHeader2.connect(
                R.id.item_studentlist_student_simple_student_title, ConstraintSet.TOP,
                hLineId, ConstraintSet.BOTTOM, defaultPaddingBy2)

        constraintSetForHeader2.applyTo(addCl)

        cl!!.addView(addCl)

        val constraintSetForHeader = ConstraintSet()
        constraintSetForHeader.clone(cl)


        if (teacherAdded!!) {

            // [Add CL Student ] always on top to top parent.
            constraintSetForHeader.connect(
                    addCl.id, ConstraintSet.TOP,
                    currentTop, ConstraintSet.BOTTOM, defaultPaddingBy2)

            // [ Add CL [Teachers] ] always start to start parent.
            constraintSetForHeader.connect(
                    addCl.id, ConstraintSet.START,
                    cl.id, ConstraintSet.START, defaultPaddingBy2)

        } else {

            // [Add CL Teacher ] always on top to top parent.
            constraintSetForHeader.connect(
                    addCl.id, ConstraintSet.TOP,
                    cl.id, ConstraintSet.TOP, defaultPaddingBy2)

            // [ Add CL [Teachers] ] always start to start parent.
            constraintSetForHeader.connect(
                    addCl.id, ConstraintSet.START,
                    cl.id, ConstraintSet.START, defaultPaddingBy2)
        }

        //Current Person image TOP to BOTTOM of [ Add CL ] (always)
        constraintSetForHeader.connect(
                R.id.item_studentlist_student_simple_student_image, ConstraintSet.TOP,
                addCl.id, ConstraintSet.BOTTOM, defaultPaddingBy2)

        //Current Person title TOP to BOTTOM of [ Add CL ] (always)
        constraintSetForHeader.connect(
                R.id.item_studentlist_student_simple_student_title, ConstraintSet.TOP,
                addCl.id, ConstraintSet.BOTTOM, defaultPaddingBy2)


        constraintSetForHeader.applyTo(cl)

        //Update the top for the next
        currentTop = addCl.id

        if (role == ClazzMember.ROLE_TEACHER) {
            teacherAdded = true
        }

    }

    companion object {

        /**
         * Generates a new Fragment for a page fragment
         *
         * @return A new instance of fragment ClazzStudentListFragment.
         */
        fun newInstance(clazzUid: Long): ClazzStudentListFragment {
            val fragment = ClazzStudentListFragment()
            val args = Bundle()
            args.putLong(ARG_CLAZZ_UID, clazzUid)
            fragment.arguments = args
            return fragment
        }

        fun newInstance(args: Bundle): ClazzStudentListFragment {
            val fragment = ClazzStudentListFragment()
            fragment.arguments = args
            return fragment
        }

        val DIFF_CALLBACK2: DiffUtil.ItemCallback<PersonWithEnrollment> = object
            : DiffUtil.ItemCallback<PersonWithEnrollment>() {
            override fun areItemsTheSame(oldItem: PersonWithEnrollment,
                                         newItem: PersonWithEnrollment): Boolean {
                return oldItem.personUid == newItem.personUid
            }

            override fun areContentsTheSame(oldItem: PersonWithEnrollment,
                                            newItem: PersonWithEnrollment): Boolean {
                return oldItem == newItem
            }
        }
    }

}
