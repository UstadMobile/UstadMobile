package com.ustadmobile.port.android.view.binding

import android.widget.AdapterView
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.ustadmobile.port.android.impl.UmDropDownOption
import com.ustadmobile.port.android.view.DropDownListAutoCompleteTextView
import com.ustadmobile.port.android.view.UmOptionsAutocompleteTextView

@BindingAdapter(value=["optionItems", "selectedOption"], requireAll =  false)
fun UmOptionsAutocompleteTextView.setOptionItems(optionItems: MutableList<UmDropDownOption>?, selectedOption: Int?) {
    val optionToUse = optionItems ?: mutableListOf()
    if(optionToUse == this.dropDownOptions)
        return

    this.dropDownOptions = optionToUse

    if(selectedOption != null)
        this.selectedDropDownOptionId = selectedOption.toLong()
}

@InverseBindingAdapter(attribute = "selectedOption")
fun UmOptionsAutocompleteTextView.getSelectedOption(): Int {
    return this.selectedDropDownOptionId.toInt()
}

@BindingAdapter("selectedOptionAttrChanged")
fun UmOptionsAutocompleteTextView.setSelectedMessageIdListener(inverseBindingListener: InverseBindingListener) {
    onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id -> inverseBindingListener.onChange() }
}


@BindingAdapter("onOptionSelected")
fun UmOptionsAutocompleteTextView.setOnOptionSelected(itemSelectedListener: DropDownListAutoCompleteTextView.OnDropDownListItemSelectedListener<UmDropDownOption>?) {
    this.onDropDownListItemSelectedListener = itemSelectedListener
}
