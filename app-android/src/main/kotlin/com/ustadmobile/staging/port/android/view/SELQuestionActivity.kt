package com.ustadmobile.staging.port.android.view


import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SELQuestionPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.SELQuestionView
import com.ustadmobile.port.android.view.UstadBaseActivity
import ru.dimorinny.floatingtextbutton.FloatingTextButton


/**
 * The SELQuestion activity - responsible for displaying the question in between SEL runs.
 */
class SELQuestionActivity : UstadBaseActivity(), SELQuestionView {

    private var toolbar: Toolbar? = null
    private var mPresenter: SELQuestionPresenter? = null

    /**
     * This method catches menu buttons/options pressed in the toolbar. Here it is making sure
     * the activity goes back when the back button is pressed.
     *
     * @param item  The item selected
     * @return  true if accounted for
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
        setContentView(R.layout.activity_sel_question)

        //Toolbar:
        toolbar = findViewById(R.id.activity_sel_question_toolbar)
        toolbar!!.title = getText(R.string.social_nomination)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        //RecyclerView
        val mRecyclerView = findViewById<RecyclerView>(
                R.id.activity_sel_question_recyclerview)
        val mRecyclerLayoutManager = LinearLayoutManager(applicationContext)
        mRecyclerView.layoutManager = mRecyclerLayoutManager

        //Call the Presenter
        mPresenter = SELQuestionPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        //FAB and its listener
        val fab = findViewById<FloatingTextButton>(R.id.activity_sel_question_fab)
        fab.setOnClickListener { v -> mPresenter!!.handleClickPrimaryActionButton() }

    }

    /**
     * Updates the question (usually called from the presenter) on the view
     *
     * @param questionText  The question string text
     */
    override fun updateQuestion(questionText: String) {
        val question = findViewById<TextView>(R.id.activity_sel_question_question)
        question.text = questionText

    }

    /**
     * Updates the question counter and totals (usually called from the presenter) on the view.
     *
     * @param qNumber   The question number from the set
     * @param tNumber   The total number of questions (usually no. of questions in a set)
     */
    override fun updateQuestionNumber(qNumber: String, tNumber: String) {
        val qNum = findViewById<TextView>(R.id.activity_sel_question_number_position)
        val qNumString = "$qNumber/$tNumber"
        qNum.text = qNumString
        toolbar!!.title = toolbar!!.title.toString() + qNumber + "/" + tNumber
    }


}
