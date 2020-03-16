package com.ustadmobile.staging.port.android.view


import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.CheckBox
import android.widget.EditText
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SELQuestionEditPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.SELQuestionEditView
import com.ustadmobile.port.android.view.UstadBaseActivity
import java.util.*


/**
 * The SELQuestionEdit activity. This Activity extends UstadBaseActivity and implements
 * SELQuestionEditView. This activity is responsible for editing and viewing an SEL question.
 */
class SELQuestionEditActivity : UstadBaseActivity(), SELQuestionEditView {

    private var mPresenter: SELQuestionEditPresenter? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Setting layout:
        setContentView(R.layout.activity_sel_question_edit)

        //Toolbar:
        val toolbar = findViewById<Toolbar>(R.id.activity_sel_question_edit_toolbar)
        setSupportActionBar(toolbar)
        Objects.requireNonNull<ActionBar>(supportActionBar).setDisplayHomeAsUpEnabled(true)

        //Recycler View:
        val mRecyclerView = findViewById<RecyclerView>(
                R.id.activity_sel_question_edit_recyclerview)
        val mRecyclerLayoutManager = LinearLayoutManager(applicationContext)
        mRecyclerView.layoutManager = mRecyclerLayoutManager

        //Call the Presenter
        mPresenter = SELQuestionEditPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_done, menu)
        return true
    }

    /**
     * This method catches menu buttons/options pressed in the toolbar. Here it is Handling
     * Action Bar button click done. - which will persist the opened / editing SEL question
     *
     * @param item  The item selected (from Menu Item)
     * @return  true if accounted for
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        val i = item.itemId
        if (i == R.id.menu_done) {//If this activity started from other activity
            val newQuestion = findViewById<TextInputLayout>(R.id.activity_sel_question_edit_question)
            val assignToAllClasses = findViewById<CheckBox>(R.id.activity_sel_question_edit_assign_to_all_classes)
            val allowMultipleNominations = findViewById<CheckBox>(R.id.activity_sel_question_edit_allow_multiple_nominations)
            mPresenter!!.handleClickDone(Objects.requireNonNull<EditText>(newQuestion.editText).getText().toString(),
                    assignToAllClasses.isChecked, allowMultipleNominations.isChecked)

            return super.onOptionsItemSelected(item)
        } else {
            return super.onOptionsItemSelected(item)
        }
    }

}
