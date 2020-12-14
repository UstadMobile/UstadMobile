package com.ustadmobile.port.android.view.binding

import android.view.LayoutInflater
import android.view.View
import androidx.databinding.BindingAdapter
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.toughra.ustadmobile.R
import com.ustadmobile.core.util.IdOption
import com.ustadmobile.core.util.ListFilterIdOption
import com.ustadmobile.core.util.OnListFilterOptionSelectedListener

/**
 * Add chips for each IdOption in the given list
 */
@BindingAdapter("chipIdOptions")
fun ChipGroup.setChipIdOptions(options: List<IdOption>) {
    val currentSelected = this.checkedChipId
    removeAllViews()
    options.forEach { option ->
        addView(LayoutInflater.from(context).inflate(R.layout.item_filter_chip, this,
                false).let { it as Chip }.also {
            it.text = option.description
            it.id = option.optionId
            it.isChecked = option.optionId == currentSelected
            it.setTag(R.id.tag_chip_optionid, option)
        })
    }

}

/**
 * Set the selected id option
 */
@BindingAdapter("selectedIdOption")
fun ChipGroup.setSelectedIdOption(optionId: Int) {
    check(optionId)
}

/**
 * Where this ChipGroup was populated using setChipIdOptions, add a OnListFilterOptionSelectedListener
 */
@BindingAdapter("onFilterOptionSelected")
fun ChipGroup.setOnFilterOptionSelected(onFilterOptionSelected: OnListFilterOptionSelectedListener) {
    setOnCheckedChangeListener { group, checkedId ->
        val selectedOptionId = group.findViewById<View>(checkedId)?.getTag(R.id.tag_chip_optionid)
            as? ListFilterIdOption
        if(selectedOptionId != null)
            onFilterOptionSelected.onListFilterOptionSelected(selectedOptionId)
    }
}
