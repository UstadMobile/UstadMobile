package com.ustadmobile.staging.port.android.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
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
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SELQuestionDetail2Presenter
import com.ustadmobile.core.db.dao.SelQuestionDao
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.SELQuestionDetail2View
import com.ustadmobile.lib.db.entities.SelQuestion
import com.ustadmobile.lib.db.entities.SelQuestionOption
import com.ustadmobile.port.android.view.UstadBaseActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SELQuestionDetail2Activity : UstadBaseActivity(), SELQuestionDetail2View {

    private var toolbar: Toolbar? = null
    private var mRecyclerView: RecyclerView? = null
    private var mPresenter: SELQuestionDetail2Presenter? = null
    private val currentQuestionUid: Long = 0
    private var questionText: EditText? = null
    private var questionType: Spinner? = null
    internal lateinit var addOptionCL: ConstraintLayout
    internal lateinit var optionsCL: ConstraintLayout


    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Setting layout:
        setContentView(R.layout.activity_sel_question_detail2)

        //Toolbar
        toolbar = findViewById(R.id.activity_sel_question_detail2_toolbar)
        toolbar!!.setTitle(R.string.edit_question)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        //Question text
        questionText = findViewById(R.id.activity_sel_question_detail2_question_name)

        //Quetion type
        questionType = findViewById(R.id.activity_sel_question_detail2_question_type_spinner)

        //Options CL
        optionsCL = findViewById(R.id.activity_sel_question_detail2_options_cl)

        //Add Option button
        addOptionCL = findViewById(R.id.activity_sel_question_detail2_add_question_cl)

        addOptionCL.setOnClickListener { view -> handleClickAddOption() }

        //Recycler View:
        mRecyclerView = findViewById(
                R.id.activity_sel_question_detail2_type_multi_choice_options_recyclerview)
        val mRecyclerLayoutManager = LinearLayoutManager(applicationContext)
        mRecyclerView!!.layoutManager = mRecyclerLayoutManager

        //Presenter
        mPresenter = SELQuestionDetail2Presenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        questionText!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun afterTextChanged(editable: Editable) {
                mPresenter!!.updateQuestionTitle(editable.toString())
            }
        })

    }


    override fun setQuestionTypePresets(presets: Array<String>) {
        runOnUiThread {
            val adapter = ArrayAdapter(applicationContext,
                    android.R.layout.simple_spinner_item, presets)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            questionType!!.adapter = adapter

            //Set listener
            setQuestionTypeListener()

        }

    }

    /**
     * Creates the options on the toolbar - specifically the Done tick menu item
     * @param menu  The menu options
     * @return  true. always.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_done, menu)
        return true
    }

    /**
     * Handles Action Bar menu button click.
     * @param item  The MenuItem clicked.
     * @return  Boolean if handled or not.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }


        // Handle item selection
        val i = item.itemId
        //If this activity started from other activity
        if (i == R.id.menu_done) {
            mPresenter!!.handleClickDone()

            return super.onOptionsItemSelected(item)
        } else {
            return super.onOptionsItemSelected(item)
        }
    }

    override fun setQuestionOptionsProvider(factory: DataSource.Factory<Int, SelQuestionOption>) {
        val recyclerAdapter = SELQuestionOptionRecyclerAdapter(DIFF_CALLBACK,
                applicationContext,
                this,
                mPresenter!!)

        // get the provider, set , observe, etc.
        // A warning is expected
        val data = LivePagedListBuilder(factory, 20).build()
        //Observe the data:

        //Observe the data:
        val thisP = this
        GlobalScope.launch(Dispatchers.Main) {
            data.observe(thisP,
                    Observer<PagedList<SelQuestionOption>> { recyclerAdapter.submitList(it) })
        }
        //set the adapter
        mRecyclerView!!.setAdapter(recyclerAdapter)
    }

    override fun setQuestionText(questionTextString: String) {
        runOnUiThread { questionText!!.setText(questionTextString) }

    }

    override fun setQuestionType(type: Int) {
        when (type) {
            SelQuestionDao
                    .SEL_QUESTION_TYPE_NOMINATION -> {
                showQuestionOptions(false)
                questionType!!.setSelection(type)
            }
            SelQuestionDao.SEL_QUESTION_TYPE_MULTI_CHOICE -> {
                showQuestionOptions(true)
                questionType!!.setSelection(type)
            }
            SelQuestionDao.SEL_QUESTION_TYPE_FREE_TEXT -> {
                showQuestionOptions(false)
                questionType!!.setSelection(type)
            }
            else -> {
            }
        }
    }

    override fun handleClickDone() {
        mPresenter!!.handleClickDone()
    }

    override fun showQuestionOptions(show: Boolean) {
        addOptionCL.visibility = if (show) View.VISIBLE else View.INVISIBLE
        addOptionCL.isEnabled = show
        optionsCL.visibility = if (show) View.VISIBLE else View.INVISIBLE
        optionsCL.isEnabled = show
    }

    override fun handleQuestionTypeChange(type: Int) {
        mPresenter!!.handleQuestionTypeChange(type)
    }

    override fun handleClickAddOption() {

        //Do something
        mPresenter!!.handleClickAddOption()
        //Do something
    }

    override fun setQuestionTypeListener() {
        runOnUiThread {
            questionType!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                    handleQuestionTypeChange(i)
                }

                override fun onNothingSelected(adapterView: AdapterView<*>) {

                }
            }
        }

    }

    override fun setQuestionOnView(selQuestion: SelQuestion) {
        if (selQuestion.questionText != null)
            setQuestionText(selQuestion.questionText!!)
        if (selQuestion.questionType > 0)
            setQuestionType(selQuestion.questionType)

    }

    companion object {

        val DIFF_CALLBACK: DiffUtil.ItemCallback<SelQuestionOption> = object
            : DiffUtil.ItemCallback<SelQuestionOption>() {

            override fun areItemsTheSame(oldItem: SelQuestionOption,
                                         newItem: SelQuestionOption): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: SelQuestionOption,
                                            newItem: SelQuestionOption): Boolean {
                return oldItem.selQuestionOptionUid == newItem.selQuestionOptionUid
            }
        }
    }
}
