package com.ustadmobile.port.android.view.binding

import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.google.android.material.button.MaterialButtonToggleGroup
import com.toughra.ustadmobile.R

//Map = Map of Value -> View Id
@BindingAdapter(value = ["messageGroupOptions", "messageGroupSelectedId"])
fun MaterialButtonToggleGroup.setMessageOptions(messageGroupOptions: Map<Int, Int>?, messageGroupSelectedId: Int?) {
    if(messageGroupOptions == null || messageGroupSelectedId == null)
        return

    val viewIdToCheck = messageGroupOptions[messageGroupSelectedId]
    if(viewIdToCheck != null)
        check(viewIdToCheck)
    else
        clearChecked()

    setTag(R.id.tag_button_toggle_group_map, messageGroupOptions)
}



@InverseBindingAdapter(attribute = "messageGroupSelectedId")
@SuppressWarnings("UncheckedCast")
fun MaterialButtonToggleGroup.getSelectedOptionId(): Int {
    val map = getTag(R.id.tag_button_toggle_group_map) as? Map<Int, Int>
    val selectedId = map?.entries?.firstOrNull { it.value == this.checkedButtonId }?.key ?: 0
    return selectedId
}

@BindingAdapter("messageGroupSelectedIdAttrChanged")
fun MaterialButtonToggleGroup.setSelectedOptionChangedListener(inverseBindingListener: InverseBindingListener) {
    addOnButtonCheckedListener { group, checkedId, isChecked ->
        if(isChecked) {
            val map = getTag(R.id.tag_button_toggle_group_map) as? Map<Int, Int>
            val selectedId = map?.entries?.firstOrNull { it.value == this.checkedButtonId }?.key
            if(selectedId != null) {
                inverseBindingListener.onChange()
            }
        }
    }
}
