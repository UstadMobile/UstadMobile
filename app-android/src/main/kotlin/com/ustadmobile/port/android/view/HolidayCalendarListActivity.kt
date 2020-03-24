package com.ustadmobile.port.android.view

import android.os.Bundle
import com.toughra.ustadmobile.R


class HolidayCalendarListActivity: UstadBaseActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_listfragment_holder)
        //setSupportActionBar(findViewById(R.id.activity_listfragment_toolbar))

        if(savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .add(R.id.activity_listfragment_frame,
                            HolidayCalendarListFragment.newInstance(intent.extras))
                    .commit()
        }
    }

}