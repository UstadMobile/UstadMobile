package com.ustadmobile.staging.port.android.view

import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.ActionBar

import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SELEditPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.SELEditView
import com.ustadmobile.lib.db.entities.PersonWithPersonPicture
import com.ustadmobile.port.android.view.UstadBaseActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

import java.util.Objects

import ru.dimorinny.floatingtextbutton.FloatingTextButton


/**
 * The SELEdit activity - This Activity extends UstadBaseActivity and implements SELEditView -
 * Is responsible for the actual SEL Edit/Detail activity ie: a nomination - with student blobs and selections
 * in them. Is usually linked to a question and run through.
 */
class SELEditActivity : UstadBaseActivity(), SELEditView {

    private var toolbar: Toolbar? = null
    private var mRecyclerView: RecyclerView? = null
    private var mPresenter: SELEditPresenter? = null

    override fun setListProvider(factory: DataSource.Factory<Int, PersonWithPersonPicture>) {

        // Specify the mAdapter
        val recyclerAdapter = PeopleBlobListRecyclerAdapter(DIFF_CALLBACK, applicationContext,
                mPresenter!!)

        // get the provider, set , observe, etc.

        val data = LivePagedListBuilder(factory, 20).build()
        //Observe the data:
        val thisP = this
        GlobalScope.launch(Dispatchers.Main) {
            data.observe(thisP,
                    Observer<PagedList<PersonWithPersonPicture>> { recyclerAdapter.submitList(it) })
        }

        //set the adapter
        mRecyclerView!!.adapter = recyclerAdapter
    }

    /**
     * Updates the heading of the title in this SEL Edit Activity
     *
     * @param questionText  The question text to be put in the heading
     */
    override fun updateHeading(questionText: String) {
        val title = findViewById<TextView>(R.id.activity_sel_edit_title)
        title.text = questionText
    }

    /**
     * Updates the toolbar with question number text
     *
     * @param iNum  The current number'th of question
     * @param tNum  The total number of questions in the current SEL run through
     */
    override fun updateHeading(iNum: String, tNum: String) {
        toolbar!!.title = toolbar!!.title.toString() + " " + iNum + "/" + tNum
    }

    /**
     * For the back button on toolbar. Its item selected listener.
     *
     * @param item  The MenuItem item
     * @return  true if pressed, false if not.
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

    /**
     * In Order:
     * 1. Sets layout
     * 2. Sets toolbar
     * 3. Sets recycler view - puts a grid layout because of
     * 4. Calls the presenter and its onCreate
     * 5. Sets the floating action button and its listener
     *
     * @param savedInstanceState    The saved instance state
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Setting layout:
        setContentView(R.layout.activity_sel_edit)

        //Toolbar:
        toolbar = findViewById(R.id.activity_sel_edit_toolbar)
        toolbar!!.title = getText(R.string.social_nomination)
        setSupportActionBar(toolbar)
        Objects.requireNonNull<ActionBar>(supportActionBar).setDisplayHomeAsUpEnabled(true)

        //Recycler View:
        mRecyclerView = findViewById(
                R.id.activity_sel_edit_recyclerview)
        val mRecyclerLayoutManager = GridLayoutManager(applicationContext, 3)
        mRecyclerView!!.layoutManager = mRecyclerLayoutManager

        //Call the Presenter
        mPresenter = SELEditPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        //FAB and its listener
        val fab = findViewById<FloatingTextButton>(R.id.activity_sel_edit_fab)
        fab.setOnClickListener { v -> mPresenter!!.handleClickPrimaryActionButton() }


    }

    companion object {

        /**
         * The DIFF Callback
         */
        val DIFF_CALLBACK: DiffUtil.ItemCallback<PersonWithPersonPicture> = object
            : DiffUtil.ItemCallback<PersonWithPersonPicture>() {
            override fun areItemsTheSame(oldItem: PersonWithPersonPicture,
                                         newItem: PersonWithPersonPicture): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: PersonWithPersonPicture,
                                            newItem: PersonWithPersonPicture): Boolean {
                return oldItem.personPictureUid == newItem.personPictureUid
            }
        }
    }


}
