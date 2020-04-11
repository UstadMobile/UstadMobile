package com.ustadmobile.port.android.impl

import androidx.annotation.Keep
import com.toughra.ustadmobile.R
import com.ustadmobile.core.impl.DestinationProvider
import com.ustadmobile.core.impl.UstadDestination
import com.ustadmobile.core.view.ClazzEdit2View
import com.ustadmobile.core.view.ClazzList2View

@Keep
class ViewNameToDestMap: DestinationProvider {

    val destinationMap = mapOf(
            ClazzEdit2View.VIEW_NAME to UstadDestination(R.id.clazz_edit_dest, false),
            ClazzList2View.VIEW_NAME to UstadDestination(R.id.home_clazzlist_dest, true))

    override val navControllerViewId: Int
        get() = R.id.activity_main_navhost_fragment

    override fun lookupDestinationName(viewName: String) = destinationMap[viewName]

    override fun lookupDestinationById(destinationId: Int) = destinationMap.values.firstOrNull { it.destinationId == destinationId }
}