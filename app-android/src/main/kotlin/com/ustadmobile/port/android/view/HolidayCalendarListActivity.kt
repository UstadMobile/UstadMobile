package com.ustadmobile.port.android.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.toughra.ustadmobile.R
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.HolidayCalendar
import com.ustadmobile.port.android.view.util.CrudListActivityResultContract

fun ComponentActivity.prepareHolidayCalendarPickFromListCall(callback: (List<HolidayCalendar>?) -> Unit)
        = prepareCall(CrudListActivityResultContract(this, HolidayCalendar::class.java,
            HolidayCalendarListActivity::class.java)) {
    callback.invoke(it)
}

class HolidayCalendarListActivity: UstadListViewActivity(), UstadListViewActivityWithFab{

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = if(intent?.getStringExtra(UstadView.ARG_LISTMODE) == ListViewMode.PICKER.toString()) {
            resources.getString(R.string.select_item, resources.getString(R.string.holiday_calendar))
        }else {
            resources.getString(R.string.holiday_calendars)
        }

        if(savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .add(R.id.activity_listfragment_frame,
                            HolidayCalendarListFragment.newInstance(intent.extras))
                    .commit()
        }
    }

}