package com.ustadmobile.port.android.view.binding

import android.view.View
import android.widget.AdapterView
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.ustadmobile.lib.db.entities.CustomFieldValueOption
import com.ustadmobile.port.android.view.CustomFieldAutoCompleteTextView

@BindingAdapter(value=["customFieldOptions", "selectedCustomFieldOption"], requireAll =  false)
fun CustomFieldAutoCompleteTextView.setCustomFieldOptions(customFieldOptions: MutableList<CustomFieldValueOption>?, selectedCustomFieldOption: Long?) {
    val sortOptionsToUse = customFieldOptions ?: mutableListOf()
    if(sortOptionsToUse == this.dropDownOptions)
        return

    this.dropDownOptions = sortOptionsToUse

    if(selectedCustomFieldOption != null)
        this.selectedDropDownOptionId = selectedCustomFieldOption
}

@InverseBindingAdapter(attribute = "selectedCustomFieldOption")
fun CustomFieldAutoCompleteTextView.getSelectedMessageIdOption(): Long {
    return this.selectedDropDownOptionId
}

@BindingAdapter("selectedCustomFieldOptionAttrChanged")
fun CustomFieldAutoCompleteTextView.setSelectedMessageIdListener(inverseBindingListener: InverseBindingListener) {
    onItemClickListener = object : AdapterView.OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            inverseBindingListener.onChange()
        }
    }
}
