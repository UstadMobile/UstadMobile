package com.ustadmobile.port.android.view

import android.content.Context
import android.util.AttributeSet
import android.widget.AdapterView
import androidx.appcompat.widget.AppCompatSpinner

class SelectableSpinner(context: Context, attrs: AttributeSet) : AppCompatSpinner(context, attrs) {

    lateinit var listener: AdapterView.OnItemSelectedListener

    override fun setSelection(position: Int) {
        super.setSelection(position)

        if (position == getSelectedItemPosition()) {
            listener.onItemSelected(null, null, position, 0)
        }
    }

    override fun setOnItemSelectedListener(listener: AdapterView.OnItemSelectedListener?) {
        this.listener = listener!!
    }
}
