package com.ustadmobile.port.android.view

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ActivitySettings2Binding
import com.ustadmobile.core.controller.SettingsPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.SettingsView

class SettingsActivity : UstadBaseActivity(), SettingsView {

    internal lateinit var mPresenter: SettingsPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dataBinding = DataBindingUtil.setContentView<ActivitySettings2Binding>(this,
                R.layout.activity_settings2)

        //Toolbar
        val toolbar = findViewById<Toolbar>(R.id.activity_settings2_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mPresenter = SettingsPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        dataBinding.presenter = mPresenter

    }
}
