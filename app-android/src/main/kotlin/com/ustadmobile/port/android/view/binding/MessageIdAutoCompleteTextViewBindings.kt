package com.ustadmobile.port.android.view.binding

import android.view.View
import android.widget.AdapterView
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.port.android.view.MessageIdAutocompleteTextView
import com.ustadmobile.port.android.view.MessageIdSpinner

@BindingAdapter(value=["messageIdOptions", "selectedMessageIdOption"], requireAll =  false)
fun MessageIdAutocompleteTextView.setMessageIdOptions(messageIdOptions: MutableList<MessageIdOption>?, selectedMessageIdOption: Int?) {
    val sortOptionsToUse = messageIdOptions ?: mutableListOf()
    if(sortOptionsToUse == this.messageIdOptions)
        return

    this.messageIdOptions = sortOptionsToUse

    if(selectedMessageIdOption != null)
        this.selectedMessageIdOption = selectedMessageIdOption
}

@InverseBindingAdapter(attribute = "selectedMessageIdOption")
fun MessageIdAutocompleteTextView.getSelectedMessageIdOption(): Int {
    return this.selectedMessageIdOption
}

@BindingAdapter("selectedMessageIdOptionAttrChanged")
fun MessageIdAutocompleteTextView.setSelectedMessageIdListener(inverseBindingListener: InverseBindingListener) {
    onItemClickListener = object : AdapterView.OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            inverseBindingListener.onChange()
        }
    }
}


@BindingAdapter("onMessageIdOptionSelected")
fun MessageIdAutocompleteTextView.setOnMessageIdOptionSelected(itemSelectedListener: MessageIdAutocompleteTextView.OnMessageIdOptionSelectedListener?) {
    this.messageIdOptionSelectedListener = itemSelectedListener
}
