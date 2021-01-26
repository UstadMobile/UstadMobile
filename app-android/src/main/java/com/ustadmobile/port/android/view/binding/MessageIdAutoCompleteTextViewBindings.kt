package com.ustadmobile.port.android.view.binding

import android.widget.AdapterView
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.ustadmobile.core.util.IdOption
import com.ustadmobile.lib.db.entities.DateRangeMoment
import com.ustadmobile.lib.db.entities.Report
import com.ustadmobile.port.android.view.DropDownListAutoCompleteTextView
import com.ustadmobile.port.android.view.MessageIdAutoCompleteTextView

@BindingAdapter(value=["messageIdOptions", "selectedMessageIdOption"], requireAll =  false)
fun MessageIdAutoCompleteTextView.setMessageIdOptions(messageIdOptions: MutableList<IdOption>?, selectedMessageIdOption: Int?) {
    val sortOptionsToUse = messageIdOptions ?: mutableListOf()

    this.takeIf { sortOptionsToUse != this.dropDownOptions}?.dropDownOptions = sortOptionsToUse

    if(selectedMessageIdOption != null)
        this.selectedDropDownOptionId = selectedMessageIdOption.toLong()
}

@InverseBindingAdapter(attribute = "selectedMessageIdOption")
fun MessageIdAutoCompleteTextView.getSelectedMessageIdOption(): Int {
    return this.selectedDropDownOptionId.toInt()
}

@BindingAdapter("selectedMessageIdOptionAttrChanged")
fun MessageIdAutoCompleteTextView.setSelectedMessageIdListener(inverseBindingListener: InverseBindingListener) {
    onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id -> inverseBindingListener.onChange() }
}


@BindingAdapter("onMessageIdOptionSelected")
fun MessageIdAutoCompleteTextView.setOnMessageIdOptionSelected(itemSelectedListener: DropDownListAutoCompleteTextView.OnDropDownListItemSelectedListener<IdOption>?) {
    this.onDropDownListItemSelectedListener = itemSelectedListener
}

@BindingAdapter(value=["selectedDateRangeMoment", "report"])
fun MessageIdAutoCompleteTextView.setDateRangeMoment(dateRangeMoment: DateRangeMoment?, report: Report?) {
    if(dateRangeMoment == null || report == null){
        return
    }
    report.fromDate = dateRangeMoment.fromMoment.fixedTime
    report.fromRelTo = dateRangeMoment.fromMoment.relTo
    report.fromRelOffSet = dateRangeMoment.fromMoment.relOffSet
    report.fromRelUnit = dateRangeMoment.fromMoment.relUnit

    report.toDate = dateRangeMoment.toMoment.fixedTime
    report.toRelTo = dateRangeMoment.toMoment.relTo
    report.toRelOffSet = dateRangeMoment.toMoment.relOffSet
    report.toRelUnit = dateRangeMoment.toMoment.relUnit
}