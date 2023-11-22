package com.ustadmobile.port.android.view.binding

import android.text.InputFilter
import android.text.Spanned
import android.widget.EditText
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputEditText
import com.ustadmobile.lib.db.entities.*
import java.util.*


private val MS_PER_HOUR = 3600000
private val MS_PER_MIN = 60000





@BindingAdapter("scheduleText")
@Deprecated("Will be removed once clazzeditpresenter/clazzdetailoverviewpresenter are removed")
fun TextView.setScheduleText(schedule: Schedule) {
}

private fun mkGmtOffsetString(rawOffset: Int): String {
    val gmtOffset =  "${(rawOffset / MS_PER_HOUR)}:" +
            (rawOffset.rem(MS_PER_HOUR) / MS_PER_MIN).toString().padStart(2, '0')
    val plusMinSymbol = if(rawOffset >= 0) "+" else ""
    return "(GMT$plusMinSymbol$gmtOffset)"
}


@BindingAdapter("runOnClickWhenFocused")
fun TextInputEditText.setRunOnClickWhenFocused(runOnClickWhenFocused: Boolean) {
    if(runOnClickWhenFocused) {
        setOnFocusChangeListener { v, hasFocus ->
            if(hasFocus)
                v.callOnClick()
        }
    }else {
        setOnFocusChangeListener(null)
    }
}

@BindingAdapter(value = ["minValue", "maxValue"])
fun EditText.setMinMax(min: Int, max: Int){
    filters = arrayOf(InputFilterMinMax(min, max))
}

class InputFilterMinMax(private val minimumValue: Int, private val maximumValue: Int) : InputFilter {
    override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence? {
        try {
            val input = (dest.subSequence(0, dstart).toString() + source + dest.subSequence(dend, dest.length)).toInt()
            if(input in minimumValue..maximumValue) return null
        } catch (nfe: NumberFormatException) {
        }
        return ""
    }

}