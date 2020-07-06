package com.ustadmobile.port.android.view.binding

import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.toughra.ustadmobile.R
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.port.android.view.MessageIdSpinner

@BindingAdapter(value=["messageIdOptions", "selectedMessageIdOption"], requireAll =  false)
fun MessageIdSpinner.setMessageIdOptions(messageIdOptions: MutableList<MessageIdOption>?, selectedMessageIdOption: Int?) {
    val sortOptionsToUse = messageIdOptions ?: mutableListOf()
    if(sortOptionsToUse == this.messageIdOptions)
        return

    this.messageIdOptions = sortOptionsToUse

    if(selectedMessageIdOption != null)
        this.selectedMessageIdOption = selectedMessageIdOption
}

@InverseBindingAdapter(attribute = "selectedMessageIdOption")
fun MessageIdSpinner.getSelectedMessageIdOption(): Int {
    return this.selectedMessageIdOption
}

@BindingAdapter("selectedMessageIdOptionAttrChanged")
fun MessageIdSpinner.setSelectedMessageIdListener(inverseBindingListener: InverseBindingListener) {
    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
            inverseBindingListener.onChange()
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            inverseBindingListener.onChange()
        }
    }
}


@BindingAdapter("onMessageIdOptionSelected")
fun MessageIdSpinner.setOnMessageIdOptionSelected(itemSelectedListener: MessageIdSpinner.OnMessageIdOptionSelectedListener?) {
    this.messageIdOptionSelectedListener = itemSelectedListener
}
