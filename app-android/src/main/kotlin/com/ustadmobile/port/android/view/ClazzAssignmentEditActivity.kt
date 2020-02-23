package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.paging.DataSource
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ActivityClazzAssignmentEditBinding
import com.ustadmobile.core.controller.ClazzAssignmentEditPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.ClazzAssignmentEditView
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.ContentEntryWithMetrics


class ClazzAssignmentEditActivity : UstadBaseActivity(), ClazzAssignmentEditView {

    private var toolbar: Toolbar? = null
    private var mPresenter: ClazzAssignmentEditPresenter? = null
    private var assignment : ClazzAssignment? = null
    private var rootView : ActivityClazzAssignmentEditBinding ? = null


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

    private fun handleClickDone(){
        rootView?.clazzassignment?.let { mPresenter?.handleSaveAssignment(it) }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Setting layout:
        rootView = ActivityClazzAssignmentEditBinding.inflate(
                LayoutInflater.from(applicationContext), null, false)

        //Toolbar:
        toolbar = rootView?.activityClazzAssignmentEditToolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)


        //Call the Presenter
        mPresenter = ClazzAssignmentEditPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

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

    companion object {


    }

    override fun setListProvider(factory: DataSource.Factory<Int, ContentEntryWithMetrics>) {
        //TODO
    }

    override fun setClazzAssignment(clazzAssignment: ClazzAssignment) {
        rootView?.clazzassignment = clazzAssignment
        rootView?.presenter = mPresenter
    }
}
