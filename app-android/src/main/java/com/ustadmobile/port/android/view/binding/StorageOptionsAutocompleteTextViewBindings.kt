package com.ustadmobile.port.android.view.binding

import android.widget.AdapterView
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.ustadmobile.core.impl.ContainerStorageDir
import com.ustadmobile.port.android.view.DropDownListAutoCompleteTextView
import com.ustadmobile.port.android.view.IdOptionAutoCompleteTextView
import com.ustadmobile.port.android.view.StorageOptionsAutocompleteTextView

@BindingAdapter(value=["storageOptions", "selectedStorageOption"], requireAll =  false)
fun StorageOptionsAutocompleteTextView.setStorageOptions(storageOptions: MutableList<ContainerStorageDir>?, selectedStorageOption: Int?) {
    val optionToUse = storageOptions ?: mutableListOf()
    if(optionToUse == this.dropDownOptions)
        return

    this.dropDownOptions = optionToUse

    if(selectedStorageOption != null)
        this.selectedDropDownOptionId = selectedStorageOption.toLong()
}

@InverseBindingAdapter(attribute = "selectedStorageOption")
fun IdOptionAutoCompleteTextView.getSelectedStorageOption(): Int {
    return this.selectedDropDownOptionId.toInt()
}

@BindingAdapter("selectedStorageOptionAttrChanged")
fun StorageOptionsAutocompleteTextView.setSelectedMessageIdListener(inverseBindingListener: InverseBindingListener) {
    onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id -> inverseBindingListener.onChange() }
}


@BindingAdapter("onStorageOptionSelected")
fun StorageOptionsAutocompleteTextView.setOnStorageOptionSelected(itemSelectedListener: DropDownListAutoCompleteTextView.OnDropDownListItemSelectedListener<ContainerStorageDir>?) {
    this.onDropDownListItemSelectedListener = itemSelectedListener
}
