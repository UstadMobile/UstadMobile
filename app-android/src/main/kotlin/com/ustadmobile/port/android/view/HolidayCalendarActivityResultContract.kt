package com.ustadmobile.port.android.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.ustadmobile.lib.db.entities.HolidayCalendar
import com.ustadmobile.port.android.view.util.CrudActivityResultContract
import kotlinx.serialization.json.Json
import kotlinx.serialization.list

class HolidayCalendarActivityResultContract(context: Context) : CrudActivityResultContract<HolidayCalendar>(context,
        HolidayCalendarListActivity::class.java, HolidayCalendarListActivity::class.java) {

    override fun parseResult(resultCode: Int, intent: Intent?): List<HolidayCalendar>? {
        if(resultCode != Activity.RESULT_OK)
            return null

        val jsonStr = intent?.extras?.getString(RESULT_EXTRA_KEY) ?: return null
        return Json.parse(HolidayCalendar.serializer().list, jsonStr)
    }

    companion object {

        const val RESULT_EXTRA_KEY = "HolidayCalendarResult"

    }
}
