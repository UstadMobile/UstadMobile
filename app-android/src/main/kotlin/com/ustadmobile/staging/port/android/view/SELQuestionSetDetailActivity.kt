package com.ustadmobile.staging.port.android.view

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SELQuestionSetDetailPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.SELQuestionSetDetailView
import com.ustadmobile.lib.db.entities.SelQuestion
import com.ustadmobile.port.android.view.UstadBaseActivity
import ru.dimorinny.floatingtextbutton.FloatingTextButton
import java.util.Objects

class SELQuestionSetDetailActivity : UstadBaseActivity(), SELQuestionSetDetailView {

    private var toolbar: Toolbar? = null
    private var mPresenter: SELQuestionSetDetailPresenter? = null
    private var mRecyclerView: RecyclerView? = null


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
        setContentView(R.layout.activity_sel_question_set_detail)

        //Toolbar:
        toolbar = findViewById(R.id.activity_sel_question_set_detail_toolbar)
        toolbar!!.title = getText(R.string.sel_question_set)
        setSupportActionBar(toolbar)
        Objects.requireNonNull<ActionBar>(supportActionBar).setDisplayHomeAsUpEnabled(true)

        //RecyclerView
        mRecyclerView = findViewById(
                R.id.activity_sel_question_set_detail_recyclerview)
        val mRecyclerLayoutManager = LinearLayoutManager(applicationContext)
        mRecyclerView!!.layoutManager = mRecyclerLayoutManager

        //Call the Presenter
        mPresenter = SELQuestionSetDetailPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        //FAB and its listener
        val fab = findViewById<FloatingTextButton>(R.id.activity_sel_question_set_detail_fab)
        fab.setOnClickListener { v -> mPresenter!!.handleClickPrimaryActionButton() }

    }

    override fun setListProvider(factory: DataSource.Factory<Int, SelQuestion>) {

        val recyclerAdapter = SELQuestionRecyclerAdapter(DIFF_CALLBACK, applicationContext,
                this, mPresenter!!)

        // get the provider, set , observe, etc.
        val data = LivePagedListBuilder(factory, 20).build()
        //Observe the data:
        runOnUiThread(Runnable {
            data.observe(this, Observer<PagedList<SelQuestion>> { recyclerAdapter.submitList(it) })
        })

        //set the adapter
        mRecyclerView!!.adapter = recyclerAdapter
    }

    override fun updateToolbarTitle(title: String) {
        toolbar!!.title = title
    }

    companion object {

        /**
         * The DIFF CALLBACK
         */
        val DIFF_CALLBACK: DiffUtil.ItemCallback<SelQuestion> = object : DiffUtil.ItemCallback<SelQuestion>() {
            override fun areItemsTheSame(oldItem: SelQuestion,
                                         newItem: SelQuestion): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: SelQuestion,
                                            newItem: SelQuestion): Boolean {
                return oldItem == newItem
            }
        }
    }
}
