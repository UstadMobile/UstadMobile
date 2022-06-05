package com.ustadmobile.core.controller

import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.TimeZoneListView
import com.ustadmobile.lib.db.entities.Person
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import org.kodein.di.DI

/**
 * The Time Zone List presenter is a little different because it does not really control any of the
 * data that the user sees. The data is provided by the underlying platform instead.
 *
 * The finishWithResult will save a string of the selected timezone id to the backstack
 */
class TimeZoneListPresenter(context: Any, args: Map<String, String>, view: TimeZoneListView, di: DI)
    : UstadBaseController<TimeZoneListView>(context, args, view, di) {


    fun handleClickTimeZone(timeZoneId: String) {
        finishWithResult(timeZoneId)
    }

    companion object {

        const val RESULT_TIMEZONE_KEY = "timezone"

    }

}