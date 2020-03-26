package com.ustadmobile.port.android.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.toughra.ustadmobile.R
import com.ustadmobile.lib.db.entities.HolidayCalendar
import com.ustadmobile.lib.db.entities.Schedule
import com.ustadmobile.port.android.view.util.CrudEditActivityResultContract
import com.ustadmobile.port.android.view.util.CrudListActivityResultContract

fun ComponentActivity.prepareHolidayCalendarPickFromListCall(callback: (List<HolidayCalendar>?) -> Unit)
        = prepareCall(CrudListActivityResultContract(this, HolidayCalendar::class.java,
            HolidayCalendarListActivity::class.java)) {
    callback.invoke(it)
}



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