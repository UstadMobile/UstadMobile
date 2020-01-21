package com.ustadmobile.staging.port.android.view

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SettingsPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.SettingsView
import com.ustadmobile.port.android.view.UstadBaseActivity

class SettingsActivity : UstadBaseActivity(), SettingsView {

    private var toolbar: Toolbar? = null
    private lateinit var mPresenter: SettingsPresenter
    private lateinit var userCL: ConstraintLayout
    private lateinit var groupCL: ConstraintLayout
    private lateinit var locationCL: ConstraintLayout


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
        setContentView(R.layout.activity_settings)

        //Toolbar:
        toolbar = findViewById(R.id.activity_settings_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        //Call the Presenter

        mPresenter = SettingsPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        userCL = findViewById(R.id.activity_settings_users_cl)
        groupCL = findViewById(R.id.activity_settings_groups_cl)
        locationCL = findViewById(R.id.activity_settings_locations_cl)


        userCL.setOnClickListener { mPresenter.goToPeopleList() }
        groupCL.setOnClickListener { mPresenter.goToGroupsList() }
        locationCL.setOnClickListener { mPresenter.goToLocationsList() }
    }
}
