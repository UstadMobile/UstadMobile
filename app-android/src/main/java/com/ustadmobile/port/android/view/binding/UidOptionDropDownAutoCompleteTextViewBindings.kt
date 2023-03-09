package com.ustadmobile.port.android.view.binding

import android.view.View
import android.widget.AdapterView
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.ustadmobile.core.util.UidOption
import com.ustadmobile.port.android.view.UidOptionAutoCompleteTextView

@BindingAdapter(value=["uidOptions", "selectedUidOption"], requireAll =  false)
fun UidOptionAutoCompleteTextView.setUidOptions(uidOptions: MutableList<UidOption>?, selectedUidOption: Long?) {
    val sortOptionsToUse = uidOptions ?: mutableListOf()
    if(sortOptionsToUse == this.dropDownOptions)
        return

    this.dropDownOptions = sortOptionsToUse

    if(selectedUidOption != null)
        this.selectedDropDownOptionId = selectedUidOption
}

@InverseBindingAdapter(attribute = "selectedUidOption")
fun UidOptionAutoCompleteTextView.getSelectedUidOption(): Long {
    return this.selectedDropDownOptionId
}

@BindingAdapter("selectedUidOptionAttrChanged")
fun UidOptionAutoCompleteTextView.setSelectedMessageIdListener(inverseBindingListener: InverseBindingListener) {
    onItemClickListener = object : AdapterView.OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            inverseBindingListener.onChange()
        }
    }
}
