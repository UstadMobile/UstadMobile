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
import com.ustadmobile.core.controller.SELQuestionSetsPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.SELQuestionSetsView
import com.ustadmobile.lib.db.entities.SELQuestionSetWithNumQuestions
import com.ustadmobile.port.android.view.UstadBaseActivity
import ru.dimorinny.floatingtextbutton.FloatingTextButton
import java.util.*

class SELQuestionSetsActivity : UstadBaseActivity(), SELQuestionSetsView {

    private var toolbar: Toolbar? = null
    private var mPresenter: SELQuestionSetsPresenter? = null
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
        setContentView(R.layout.activity_sel_question_sets)

        //Toolbar:
        toolbar = findViewById(R.id.activity_sel_question_sets_toolbar)
        toolbar!!.title = getText(R.string.sel_question_set)
        setSupportActionBar(toolbar)
        Objects.requireNonNull<ActionBar>(supportActionBar).setDisplayHomeAsUpEnabled(true)

        //RecyclerView
        mRecyclerView = findViewById(
                R.id.activity_sel_question_sets_recyclerview)
        val mRecyclerLayoutManager = LinearLayoutManager(applicationContext)
        mRecyclerView!!.layoutManager = mRecyclerLayoutManager

        //Call the Presenter
        mPresenter = SELQuestionSetsPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        //FAB and its listener
        val fab = findViewById<FloatingTextButton>(R.id.activity_sel_question_sets_fab)
        fab.setOnClickListener { v -> mPresenter!!.handleClickPrimaryActionButton() }

    }

    override fun setListProvider(factory: DataSource.Factory<Int, SELQuestionSetWithNumQuestions>) {
        val recyclerAdapter = SELQuestionSetListRecyclerAdapter(DIFF_CALLBACK, mPresenter!!,
                applicationContext)

        // get the provider, set , observe, etc.

        val data = LivePagedListBuilder(factory, 20).build()
        //Observe the data:
        data.observe(this,
                Observer<PagedList<SELQuestionSetWithNumQuestions>> { recyclerAdapter.submitList(it) })

        //set the adapter
        mRecyclerView!!.adapter = recyclerAdapter
    }

    companion object {

        /**
         * The DIFF CALLBACK
         */
        val DIFF_CALLBACK: DiffUtil.ItemCallback<SELQuestionSetWithNumQuestions> = object
            : DiffUtil.ItemCallback<SELQuestionSetWithNumQuestions>() {
            override fun areItemsTheSame(oldItem: SELQuestionSetWithNumQuestions,
                                         newItem: SELQuestionSetWithNumQuestions): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: SELQuestionSetWithNumQuestions,
                                            newItem: SELQuestionSetWithNumQuestions): Boolean {
                return oldItem == newItem
            }
        }
    }
}
