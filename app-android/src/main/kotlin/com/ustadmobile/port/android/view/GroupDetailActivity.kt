package com.ustadmobile.port.android.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.app.ActionBar
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
import com.ustadmobile.core.controller.GroupDetailPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.view.GroupDetailView
import com.ustadmobile.lib.db.entities.PersonGroup
import com.ustadmobile.lib.db.entities.PersonWithEnrollment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.dimorinny.floatingtextbutton.FloatingTextButton
import java.util.*

class GroupDetailActivity : UstadBaseActivity(), GroupDetailView {

    private var toolbar: Toolbar? = null
    private var mPresenter: GroupDetailPresenter? = null
    private var mRecyclerView: RecyclerView? = null
    private var title: EditText? = null


    /**
     * This method catches menu buttons/options pressed in the toolbar. Here it is making sure
     * the activity goes back when the back button is pressed.
     *
     * @param item The item selected
     * @return true if accounted for
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
        setContentView(R.layout.activity_group_detail)

        //Toolbar:
        toolbar = findViewById(R.id.activity_group_detail_toolbar)
        toolbar!!.title = getText(R.string.new_group)
        setSupportActionBar(toolbar)
        Objects.requireNonNull<ActionBar>(supportActionBar).setDisplayHomeAsUpEnabled(true)

        //RecyclerView
        mRecyclerView = findViewById(
                R.id.activity_group_detail_recyclerview)
        val mRecyclerLayoutManager = LinearLayoutManager(applicationContext)
        mRecyclerView!!.layoutManager = mRecyclerLayoutManager

        title = findViewById(R.id.activity_group_detail_name)
        title!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                mPresenter!!.updateGroupName(s.toString())
            }
        })

        val addPersonCL = findViewById<ConstraintLayout>(R.id.activity_group_detail_add_cl)
        addPersonCL.setOnClickListener { v -> mPresenter!!.handleClickAddMember() }
        //Call the Presenter
        mPresenter = GroupDetailPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        //FAB and its listener
        val fab = findViewById<FloatingTextButton>(R.id.activity_group_detail_fab)

        fab.setOnClickListener { v -> mPresenter!!.handleClickDone() }

    }

    override fun setListProvider(factory: DataSource.Factory<Int, PersonWithEnrollment>) {

        val boundaryCallback = UmAccountManager.getRepositoryForActiveAccount(applicationContext)
                .personGroupMemberDaoBoundaryCallbacks.findAllPersonWithEnrollmentWithGroupUid(factory)

        val recyclerAdapter = GroupDetailRecyclerAdapter(DIFF_CALLBACK, mPresenter!!, this,
                applicationContext)

        // get the provider, set , observe, etc.
        val data = LivePagedListBuilder(factory, 20)
                .setBoundaryCallback(boundaryCallback)
                .build()
        //Observe the data:
        val thisP = this
        GlobalScope.launch(Dispatchers.Main) {
            data.observe(thisP,
                    Observer<PagedList<PersonWithEnrollment>> { recyclerAdapter.submitList(it) })
            //set the adapter
            mRecyclerView!!.adapter = recyclerAdapter
        }

    }

    override fun updateGroupOnView(group: PersonGroup) {
        var groupName: String? = ""
        if (group != null) {
            if (group.groupName != null) {
                groupName = group.groupName
            }
        }
        val finalGroupName = groupName
        runOnUiThread {
            title!!.setText(finalGroupName)
            if (!finalGroupName!!.isEmpty()) {
                toolbar!!.title = finalGroupName
            }
        }

    }

    companion object {

        /**
         * The DIFF CALLBACK
         */
        val DIFF_CALLBACK: DiffUtil.ItemCallback<PersonWithEnrollment> = object : DiffUtil.ItemCallback<PersonWithEnrollment>() {
            override fun areItemsTheSame(oldItem: PersonWithEnrollment,
                                         newItem: PersonWithEnrollment): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: PersonWithEnrollment,
                                            newItem: PersonWithEnrollment): Boolean {
                return oldItem == newItem
            }
        }
    }


}
