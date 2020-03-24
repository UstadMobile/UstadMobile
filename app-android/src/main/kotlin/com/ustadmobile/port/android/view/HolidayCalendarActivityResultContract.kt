package com.ustadmobile.port.android.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.ustadmobile.core.view.GetResultMode
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.HolidayCalendar
import kotlinx.serialization.json.Json
import kotlinx.serialization.list

class HolidayCalendarActivityResultContract(val context: Context) : ActivityResultContract<GetResultMode, List<HolidayCalendar>?>() {

    override fun createIntent(input: GetResultMode): Intent {
        return when(input) {
            GetResultMode.FROMLIST -> Intent(context, HolidayCalendarListActivity::class.java).apply {
                putExtra(UstadView.ARG_LISTMODE, ListViewMode.PICKER.toString())
            }

            GetResultMode.CREATENEW -> Intent(context, HolidayCalendarListActivity::class.java).apply {
                putExtra(UstadView.ARG_LISTMODE, ListViewMode.PICKER.toString())
            }
        }
    }

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
