package com.ustadmobile.port.android.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ActivityClazzAssignmentEditBinding
import com.ustadmobile.core.controller.ClazzAssignmentEditPresenter
import com.ustadmobile.core.controller.HomePresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ClazzAssignmentEditView
import com.ustadmobile.core.view.ContentEntryListView
import com.ustadmobile.core.view.ContentEntryListView.Companion.EXTRA_RESULT_CONTENTENTRY
import com.ustadmobile.core.view.ContentEntryListViewMode
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.ClazzAssignmentContentEntryJoinWithContentEntry
import com.ustadmobile.lib.db.entities.ContentEntry
import kotlinx.serialization.json.Json

class ClazzAssignmentEditActivity : UstadBaseActivity(), ClazzAssignmentEditView {

    private var toolbar: Toolbar? = null
    private var mPresenter: ClazzAssignmentEditPresenter? = null
    private var assignment : ClazzAssignment? = null
    private var rootView : ActivityClazzAssignmentEditBinding ? = null
    private var mRecyclerView: RecyclerView? = null
    private var idToOrderInteger: MutableMap<Long, Int>? = null


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
        //If this activity started from other activity
        if (item.itemId == R.id.menu_done) {
            handleClickDone()
            return super.onOptionsItemSelected(item)
        } else {
            return super.onOptionsItemSelected(item)
        }
    }

    private fun setGroupSpinner() {
        idToOrderInteger = HashMap()
        (idToOrderInteger as HashMap<Long, Int>)[1L] = ClazzAssignmentEditView.GRADING_NONE
        (idToOrderInteger as HashMap<Long, Int>)[2L] = ClazzAssignmentEditView.GRADING_NUMERICAL
        (idToOrderInteger as HashMap<Long, Int>)[3L] = ClazzAssignmentEditView.GRADING_LETTERS

        val options = listOf(MessageID.None, MessageID.numerical, MessageID.grading_letter)
                .map { UstadMobileSystemImpl.instance.getString(it, this) }

        val adapter = ArrayAdapter(this,
                R.layout.item_simple_spinner_gray, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    }

    private fun handleClickDone(){
        rootView?.clazzassignment?.let { mPresenter?.handleSaveAssignment(it) }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        rootView = DataBindingUtil.setContentView(this,
                        R.layout.activity_clazz_assignment_edit)

        //Toolbar:
        toolbar = rootView?.activityClazzAssignmentEditToolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //Presets
        setGroupSpinner()

        //RV:
        mRecyclerView = rootView?.activityClazzAssignmentEditContentRecyclerview
        val mRecyclerLayoutManager = LinearLayoutManager(this)
        mRecyclerView?.layoutManager = mRecyclerLayoutManager

        rootView?.activityClazzAssignmentEditAddContent?.setOnClickListener {
            handleClickAddContent()
        }

        //Call the Presenter
        mPresenter = ClazzAssignmentEditPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter?.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        rootView?.setLifecycleOwner(this)
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

    override var contentEntryList: DoorMutableLiveData<List<ClazzAssignmentContentEntryJoinWithContentEntry>>? = null
        get() = field
        set(value) {
            val recyclerAdapter = ContentEntryListRecyclerAdapter(DIFF_CALLBACK_CONTENT_ENTRY_JOIN_WITH_CONTENT_ENTRY)
            value?.observe(this, Observer { list -> recyclerAdapter.submitList(list)})
                    mRecyclerView?.adapter = recyclerAdapter
                    field = value
        }

    override fun setClazzAssignment(clazzAssignment: ClazzAssignment) {
        rootView?.clazzassignment = clazzAssignment
        rootView?.presenter = mPresenter

    }

    override fun onDestroy() {
        super.onDestroy()
        rootView = null
        mPresenter = null
        mRecyclerView = null
        mRecyclerView?.adapter = null
    }

    /**
     * Handle what happens when you click add Content button
     */
    private fun handleClickAddContent(){
        val args = mapOf(
                ContentEntryListView.ARG_VIEWMODE to  ContentEntryListViewMode.PICKER.toString(),
                UstadView.ARG_CONTENT_ENTRY_UID to HomePresenter.MASTER_SERVER_ROOT_ENTRY_UID.toString(),
                ContentEntryListView.ARG_LIBRARIES_CONTENT to "")
        val bundle = UMAndroidUtil.mapToBundle(args)?: Bundle()
        val startIntent = Intent(this, ContentEntryListActivity::class.java)
        startIntent.putExtras(bundle)
        startActivityForResult(startIntent, REQUEST_CODE_CONTENT_PICKER)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == REQUEST_CODE_CONTENT_PICKER && resultCode == Activity.RESULT_OK
                && data != null ){
            val json = data.getStringExtra(EXTRA_RESULT_CONTENTENTRY)
            val contentEntry = Json.parse(ContentEntry.serializer(), json)
            mPresenter?.handleContentEntryAdded(contentEntry)
        }
    }

    companion object{

        const val REQUEST_CODE_CONTENT_PICKER = 212
        /**
         * The DIFF Callback.
         */
        val DIFF_CALLBACK_CONTENT_ENTRY_JOIN_WITH_CONTENT_ENTRY: DiffUtil.ItemCallback<ClazzAssignmentContentEntryJoinWithContentEntry> = object
            : DiffUtil.ItemCallback<ClazzAssignmentContentEntryJoinWithContentEntry>() {
            override fun areItemsTheSame(oldItem: ClazzAssignmentContentEntryJoinWithContentEntry,
                                         newItem: ClazzAssignmentContentEntryJoinWithContentEntry): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: ClazzAssignmentContentEntryJoinWithContentEntry,
                                            newItem: ClazzAssignmentContentEntryJoinWithContentEntry): Boolean {
                return oldItem == newItem
            }
        }

        /**
         * The DIFF Callback.
         */
        val DIFF_CALLBACK_CONTENT: DiffUtil.ItemCallback<ContentEntry> = object
            : DiffUtil.ItemCallback<ContentEntry>() {
            override fun areItemsTheSame(oldItem: ContentEntry,
                                         newItem: ContentEntry): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: ContentEntry,
                                            newItem: ContentEntry): Boolean {
                return oldItem == newItem
            }
        }
    }
}
