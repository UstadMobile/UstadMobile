package com.ustadmobile.port.android.view

import android.os.Bundle
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.toughra.ustadmobile.R

abstract class UstadListViewActivity() : UstadBaseActivity(), UstadActivityWithFab {

    override val activityFloatingActionButton: ExtendedFloatingActionButton?
        get() = findViewById(R.id.activity_listfragment_fab)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_listfragment_holder)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }
}