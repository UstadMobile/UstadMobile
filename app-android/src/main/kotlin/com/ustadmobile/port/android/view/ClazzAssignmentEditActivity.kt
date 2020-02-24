package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.paging.DataSource
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ActivityClazzAssignmentEditBinding
import com.ustadmobile.core.controller.ClazzAssignmentEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ClazzAssignmentEditView
import com.ustadmobile.core.view.ClazzListView
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.ContentEntryWithMetrics

class ClazzAssignmentEditActivity : UstadBaseActivity(), ClazzAssignmentEditView {

    private var toolbar: Toolbar? = null
    private var mPresenter: ClazzAssignmentEditPresenter? = null
    private var assignment : ClazzAssignment? = null
    private var rootView : ActivityClazzAssignmentEditBinding ? = null
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
        rootView?.activityClazzAssignmentEditGradingSpinner?.adapter = adapter
        rootView?.activityClazzAssignmentEditGradingSpinner?.setSelection(0)
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
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        //Presets
        setGroupSpinner()

        //Call the Presenter
        mPresenter = ClazzAssignmentEditPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

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

    companion object {}

    override fun setListProvider(factory: DataSource.Factory<Int, ContentEntryWithMetrics>) {
        //TODO
    }

    override fun setClazzAssignment(clazzAssignment: ClazzAssignment) {
        rootView?.clazzassignment = clazzAssignment
        rootView?.presenter = mPresenter
    }

    override fun onDestroy() {
        super.onDestroy()
        rootView = null
        mPresenter = null
    }
}
