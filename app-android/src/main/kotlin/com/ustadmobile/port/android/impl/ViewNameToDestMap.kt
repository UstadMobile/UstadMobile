package com.ustadmobile.port.android.impl

import androidx.annotation.Keep
import com.toughra.ustadmobile.R
import com.ustadmobile.core.impl.DestinationProvider
import com.ustadmobile.core.impl.UstadDestination
import com.ustadmobile.core.view.ClazzEdit2View
import com.ustadmobile.core.view.ClazzList2View
import com.ustadmobile.core.view.HolidayCalendarEditView
import com.ustadmobile.core.view.TimeZoneEntityListView
import com.ustadmobile.staging.core.view.HolidayCalendarListView

@Keep
class ViewNameToDestMap: DestinationProvider {

    val destinationMap = mapOf(
            ClazzEdit2View.VIEW_NAME to UstadDestination(R.id.clazz_edit_dest, false),
            ClazzList2View.VIEW_NAME to UstadDestination(R.id.home_clazzlist_dest, true),
            HolidayCalendarListView.VIEW_NAME to UstadDestination(R.id.holidaycalendar_list_dest, true),
            HolidayCalendarEditView.VIEW_NAME to UstadDestination(R.id.holidaycalendar_edit_dest, false),
            TimeZoneEntityListView.VIEW_NAME to UstadDestination(R.id.timezoneentity_list_dest, false))

    override val navControllerViewId: Int
        get() = R.id.activity_main_navhost_fragment

    override fun lookupDestinationName(viewName: String) = destinationMap[viewName]

    override fun lookupDestinationById(destinationId: Int) = destinationMap.values.firstOrNull { it.destinationId == destinationId }
}