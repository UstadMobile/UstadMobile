package com.ustadmobile.port.android.view.ext

import android.widget.EditText
import com.toughra.ustadmobile.R
import java.util.*

val EditText.calendar: Calendar
    get(){
        val currentProps = getTag(R.id.tag_calendar) as Calendar?
        if(currentProps != null)
            return currentProps

        val newDateTime = Calendar.getInstance()
        setTag(R.id.tag_calendar, newDateTime)
        return newDateTime
    }