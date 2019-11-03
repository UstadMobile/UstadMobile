package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.ClazzDetailEnrollStudentPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.ClazzDetailEnrollStudentView
import com.ustadmobile.core.view.ClazzDetailEnrollStudentView.Companion.ARG_NEW_PERSON_TYPE
import com.ustadmobile.core.view.ClazzListView.Companion.ARG_CLAZZ_UID
import com.ustadmobile.core.view.GroupDetailView.Companion.GROUP_UID
import com.ustadmobile.lib.db.entities.ClazzMember
import com.ustadmobile.lib.db.entities.PersonWithEnrollment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.dimorinny.floatingtextbutton.FloatingTextButton

/**
 * Clazz detail Enroll Student - Enrollment activity.
 * Gets called when "Add Student" is clicked on both the current Clazz selected as well as
 * from the People Bottom Navigation.
 */
class ClazzDetailEnrollStudentActivity : UstadBaseActivity(), ClazzDetailEnrollStudentView {

    //Toolbar
    private var toolbar: Toolbar? = null

    //Recycler View
    private var mRecyclerView: RecyclerView? = null
    private var mRecyclerLayoutManager: RecyclerView.LayoutManager? = null

    //Presenter
    private var mPresenter: ClazzDetailEnrollStudentPresenter? = null

    private var currentClazzUid: Long = 0
    private var currentRole: Int = 0

    //PersonGroup enrollment
    private var groupUid: Long = 0

    private var groupEnrollment = false

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Set Layout
        setContentView(R.layout.activity_clazz_detail_enroll_student)

        //Toolbar:
        toolbar = findViewById(R.id.activity_clazz_detail_enroll_student_toolbar)

        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        //Get the clazz Uid from the arguments
        if (intent.hasExtra(ARG_CLAZZ_UID)) {
            currentClazzUid = intent.extras!!.get(ARG_CLAZZ_UID).toString().toLong()
        }

        if (intent.hasExtra(ARG_NEW_PERSON_TYPE)) {
            currentRole = intent.extras!!.get(ARG_NEW_PERSON_TYPE).toString().toInt()
        }

        //PersonGroup enrollment
        if (intent.hasExtra(GROUP_UID)) {
            groupUid = intent.extras!!.get(GROUP_UID).toString().toLong()
            if (groupUid != 0L) {
                groupEnrollment = true
            }
        }

        val enrollNewClazzMemberButton = findViewById<Button>(R.id.activity_clazz_Detail_enroll_student_new)

        if (currentRole == ClazzMember.ROLE_TEACHER) {
            toolbar!!.title = getText(R.string.add_teacher)
            enrollNewClazzMemberButton.text = getText(R.string.enroll_new_teacher)
        } else if (currentRole == ClazzMember.ROLE_STUDENT) {
            toolbar!!.title = getText(R.string.add_student)
            enrollNewClazzMemberButton.text = getText(R.string.enroll_new_student)
        } else if (groupUid != 0L) {
            //Add person to group
            toolbar!!.setTitle(R.string.enroll_group_member)
            enrollNewClazzMemberButton.text = getText(R.string.enroll_new_group_member)
        }

        //RecyclerView:
        mRecyclerView = findViewById(
                R.id.activity_clazz_detail_enroll_student_recycler_view)
        mRecyclerLayoutManager = LinearLayoutManager(applicationContext)
        mRecyclerView!!.layoutManager = mRecyclerLayoutManager

        //Presenter
        mPresenter = ClazzDetailEnrollStudentPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        //Enroll new student
        val newStudentButton = findViewById<Button>(R.id.activity_clazz_Detail_enroll_student_new)
        newStudentButton.setOnClickListener { v -> mPresenter!!.handleClickEnrollNewPerson() }

        //FAB
        val fab = findViewById<FloatingTextButton>(R.id.activity_clazz_detail_enroll_student_fab_done)
        fab.setOnClickListener { v -> mPresenter!!.handleClickDone() }

    }

    override fun setStudentsProvider(factory: DataSource.Factory<Int, PersonWithEnrollment>) {

        val recyclerAdapter = PersonWithEnrollmentRecyclerAdapter(DIFF_CALLBACK, applicationContext,
                this, mPresenter!!, true, true, groupEnrollment)

        val data = LivePagedListBuilder(factory, 20).build()
        //Observe the data:
        val thisP = this
        GlobalScope.launch(Dispatchers.Main) {
            data.observe(thisP,
                    Observer<PagedList<PersonWithEnrollment>> { recyclerAdapter.submitList(it) })
        }

        mRecyclerView!!.adapter = recyclerAdapter
    }

    companion object {


        // Diff callback.
        val DIFF_CALLBACK: DiffUtil.ItemCallback<PersonWithEnrollment> = object : DiffUtil.ItemCallback<PersonWithEnrollment>() {
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
