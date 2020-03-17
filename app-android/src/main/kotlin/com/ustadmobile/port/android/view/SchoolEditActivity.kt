package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ActivitySchoolDetailBinding
import com.ustadmobile.core.controller.SchoolEditPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.SchoolEditView
import com.ustadmobile.lib.db.entities.School

class SchoolEditActivity : UstadBaseActivity(), SchoolEditView {

    private var toolbar: Toolbar? = null
    private lateinit var mPresenter: SchoolEditPresenter

    private lateinit var binding: ActivitySchoolDetailBinding

    private var currentSchool : School ? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Setting layout:
        binding = DataBindingUtil.inflate<ActivitySchoolDetailBinding>(layoutInflater,
                R.layout.activity_school_detail, null,false)

        //Toolbar:
        toolbar = findViewById(R.id.activity_school_detail_toolbar)
        setSupportActionBar(toolbar)
        
        //supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        //Call the Presenter
        mPresenter = SchoolEditPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

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

    /**
     * Handles Action Bar menu button click.
     * @param item  The MenuItem clicked.
     * @return  Boolean if handled or not.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        // Handle item selection
        val i = item.itemId
        //If this activity started from other activity
        if (i == R.id.menu_done) {
            var school = currentSchool!!
            mPresenter.handleClickSave(school)
            return super.onOptionsItemSelected(item)
        } else {
            return super.onOptionsItemSelected(item)
        }
    }

    override fun setSchool(school: School) {
        binding.school = school

        val view = binding.root
        setContentView(view)
    }

    override fun setPicture(picturePath: String) {
        //TODO:
    }


}
