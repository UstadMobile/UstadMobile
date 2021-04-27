package com.ustadmobile.core.view

interface TimeZoneListView : UstadView {

    fun finishWithResult(timeZoneId: String)

    companion object {

        const val VIEW_NAME = "TimeZoneListView"
    }

}