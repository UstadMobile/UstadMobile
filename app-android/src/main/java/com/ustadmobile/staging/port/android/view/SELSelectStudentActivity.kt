package com.ustadmobile.staging.port.android.view

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner

import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.google.android.material.snackbar.Snackbar
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SELSelectStudentPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.SELSelectStudentView
import com.ustadmobile.core.view.SELSelectStudentView.Companion.ARG_STUDENT_DONE
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.port.android.view.UstadBaseActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * The activity that shows the list of students in this clazz who will be doing the SEL nominations
 */
class SELSelectStudentActivity : UstadBaseActivity(), SELSelectStudentView {
    private var mRecyclerView: RecyclerView? = null
    private var mPresenter: SELSelectStudentPresenter? = null
    private var studentDoneSnackBar: Snackbar? = null
    private var selQuestionSetSpinner: Spinner? = null
    internal lateinit var questionSetPresets: Array<String>

    override fun setSELAnswerListProvider(factory: DataSource.Factory<Int, Person>) {

        // Specify the mAdapter
        val recyclerAdapter = SimplePeopleListRecyclerAdapter(DIFF_CALLBACK, applicationContext, mPresenter!!)

        // get the provider, set , observe, etc.
        val data = LivePagedListBuilder(factory, 20).build()
        //Observe the data:
        //Observe the data:
        val thisP = this
        GlobalScope.launch(Dispatchers.Main) {
            data.observe(thisP,
                    Observer<PagedList<Person>> { recyclerAdapter.submitList(it) })
        }

        //set the adapter
        mRecyclerView!!.adapter = recyclerAdapter
    }

    override fun setQuestionSetDropdownPresets(presets: Array<String>) {
        this.questionSetPresets = presets
        val adapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, questionSetPresets)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        selQuestionSetSpinner!!.adapter = adapter
    }


    /**
     * Handles what happens when toolbar menu option selected. Here it is handling what happens when
     * back button is pressed.
     *
     * @param item  The item selected.
     * @return      true if accounted for.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Setting layout:
        setContentView(R.layout.activity_sel_select_student)

        //Toolbar:
        val toolbar = findViewById<Toolbar>(R.id.activity_sel_select_student_toolbar)
        toolbar.title = getText(R.string.social_nomination)
        setSupportActionBar(toolbar)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val cl = findViewById<ConstraintLayout>(R.id.activity_sel_select_student_cl)
        studentDoneSnackBar = Snackbar
                .make(cl, getText(R.string.sel_done_select_another_student), Snackbar.LENGTH_LONG)

        selQuestionSetSpinner = findViewById(R.id.activity_sel_select_student_sel_question_set_spinner)

        //Recycler View:
        mRecyclerView = findViewById(
                R.id.activity_sel_select_student_recyclerview)
        val mRecyclerLayoutManager = LinearLayoutManager(applicationContext)
        mRecyclerView!!.layoutManager = mRecyclerLayoutManager

        //Call the Presenter
        mPresenter = SELSelectStudentPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        //If student done in argument, show toast/snackbar
        if (intent.hasExtra(ARG_STUDENT_DONE)) {
            studentDoneSnackBar!!.show()
        }

        selQuestionSetSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                mPresenter!!.handleChangeQuestionSetSelected(id)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

    }

    companion object {

        val DIFF_CALLBACK: DiffUtil.ItemCallback<Person> = object : DiffUtil.ItemCallback<Person>() {
            override fun areItemsTheSame(oldItem: Person,
                                         newItem: Person): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: Person,
                                            newItem: Person): Boolean {
                return oldItem == newItem
            }
        }
    }


}
